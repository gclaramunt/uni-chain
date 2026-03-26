package gclaramunt.unichain

import cats.effect.Ref
import cats.effect.kernel.{Concurrent, MonadCancelThrow}
import cats.syntax.all.*
import gclaramunt.unichain.Config.{NodeConfig, nodeConfig}
import gclaramunt.unichain.blockchain.CryptoOps.pubKeyToAddress
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB

private case class ServiceState(
  lastBlock: Block,
  balances: Map[Address, BigDecimal],
  nonces: Map[Address, Long],
  memPool: Seq[Transaction]
)

class UnichainService[F[_] : MonadCancelThrow](config: NodeConfig, bOps: BlockchainOps)(refs: Ref[F, ServiceState], ledgerDB: LedgerDB[F]):

  def submitTx(tx: Transaction): F[Unit] =
    for
        isValid <- MonadCancelThrow[F].fromTry(BlockchainOps.validate(tx))
        _ <- if !isValid then
          MonadCancelThrow[F].raiseError(new RuntimeException("Invalid transaction signature"))
        else
          refs.flatModify: state =>
            val lastNonce = state.nonces.getOrElse(tx.source, -1L)
            if tx.nonce <= lastNonce then
              (state, MonadCancelThrow[F].raiseError(new RuntimeException(s"Invalid nonce: ${tx.nonce}, expected > $lastNonce")))
            else
              val srcUpdtBalance = state.balances.getOrElse(tx.source, BigDecimal(0)) - tx.amount
              val destUdptBalance = state.balances.getOrElse(tx.destination, BigDecimal(0)) + tx.amount
              if srcUpdtBalance >= 0 then
                val updatedBalances = state.balances
                  + (tx.source -> srcUpdtBalance)
                  + (tx.destination -> destUdptBalance)
                val updatedNonces = state.nonces + (tx.source -> tx.nonce)
                val updatedMemPool = state.memPool :+ tx
                if updatedMemPool.size < config.transactionsPerBlock then
                  (state.copy(balances = updatedBalances, nonces = updatedNonces, memPool = updatedMemPool),
                    ledgerDB.addTransaction(state.lastBlock.id, tx).map(_ => ()))
                else
                  bOps.newBlock(state.lastBlock, updatedMemPool).map: newBlock =>
                    val dbUpdate = ledgerDB.addBlockWithTransactions(newBlock, updatedMemPool)
                    (state.copy(lastBlock = newBlock, balances = updatedBalances, nonces = updatedNonces, memPool = Seq()), dbUpdate)
                  .fold(
                    err => (state, MonadCancelThrow[F].raiseError(new Exception(err.toString))),
                    identity
                  )
              else
                (state, MonadCancelThrow[F].raiseError(new RuntimeException("Source final balance can't be less than 0")))
    yield ()

  def addressBalance(address: Address): F[Option[BigDecimal]] =
    refs.get.map(state => state.balances.get(address))

  def lastValidBlock(): F[Block] =
    refs.get.map(state => state.lastBlock)

object UnichainService:

  def apply[F[_] : Concurrent](ledgerDb: LedgerDB[F]): F[UnichainService[F]] = apply(ledgerDb, nodeConfig)

  def apply[F[_] : Concurrent](ledgerDb: LedgerDB[F], config: NodeConfig): F[UnichainService[F]] =
    for
      bOps <- Concurrent[F].fromTry(BlockchainOps.fromConfig(config.crypto))
      lastBlock <- ledgerDb.getLastBlock
      (balances, nonces) <- buildInitialState(config)(ledgerDb.getTransactions)
      refs <- Ref.of(ServiceState(lastBlock, balances, nonces, Seq.empty[Transaction]))
    yield new UnichainService[F](config, bOps)(refs, ledgerDb)

  def buildInitialState[F[_] : Concurrent](config: NodeConfig)(txs: fs2.Stream[F, Transaction]): F[(Map[Address, BigDecimal], Map[Address, Long])] =
    val genesisAddress = pubKeyToAddress(BlockchainOps.fromConfig(config.crypto).get.publicKey)

    def updateBalance(m: Map[Address, BigDecimal], k: Address, amount: BigDecimal): Map[Address, BigDecimal] =
      val currentVal = m.getOrElse(k, BigDecimal(0))
      m + (k -> (currentVal + amount))

    txs.compile.fold((Map.empty[Address, BigDecimal], Map.empty[Address, Long])):
      case ((balances, nonces), tx) =>
        val updatedNonces = nonces + (tx.source -> math.max(nonces.getOrElse(tx.source, -1L), tx.nonce))
        val updatedBalances =
          if tx.destination == genesisAddress && tx.nonce == 0 then
            balances + (genesisAddress -> tx.amount)
          else
            val m1 = updateBalance(balances, tx.source, -tx.amount)
            updateBalance(m1, tx.destination, tx.amount)
        (updatedBalances, updatedNonces)
