package gclaramunt.unichain

import cats.effect.kernel.{Concurrent, MonadCancelThrow}
import cats.effect.{Async, Ref}
import cats.syntax.all.*
import doobie.Transactor
import gclaramunt.unichain.Config.{NodeConfig, nodeConfig}
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB


// TODO Treasury / genesis block
class UnichainService[F[_] : MonadCancelThrow](config: NodeConfig)(refs: Ref[F, (Block, Map[Address, BigDecimal], Seq[Transaction])], ledgerDB: LedgerDB[F]):
  private val bOps = BlockchainOps(config.crypto)
  def submitTx(tx: Transaction): F[Unit] =
    for {
      isValid <- MonadCancelThrow[F].fromTry(bOps.validate(tx))
      _ <- if (!isValid)
        MonadCancelThrow[F].raiseError(new RuntimeException("Invalid transaction signature"))
      else
        refs.flatModify { case (lastBlock, balances, memPool) =>
          val srcUpdtBalance = balances(tx.source) - tx.amount
          val destUdptBalance = balances.getOrElse(tx.destination, BigDecimal(0)) + tx.amount
          if (srcUpdtBalance >= 0) {
            val updatedBalances = balances + (tx.source -> srcUpdtBalance)
              + (tx.destination -> destUdptBalance)
            val updatedMemPool = memPool :+ tx
            if (updatedMemPool.size < config.transactionsPerBlock) {
              ((lastBlock, updatedBalances, updatedMemPool), ledgerDB.addTransaction(lastBlock.id, tx).map(_ => ()))
            } else {
              bOps.newBlock(lastBlock, updatedMemPool).map { newBlock =>
                val dbUpdate = for {
                  _ <- ledgerDB.addBlock(newBlock)
                  _ <- ledgerDB.addTransaction(newBlock.id, tx)
                } yield ()
                ((newBlock, updatedBalances, Seq()), dbUpdate)
              }.fold( err =>((lastBlock, balances, memPool), MonadCancelThrow[F].raiseError(err)), identity)
            }
          } else ((lastBlock, balances, memPool), MonadCancelThrow[F].raiseError(new RuntimeException("Source final balance can't be less than 0")))
        }
    } yield ()


  def addressBalance(address: Address): F[Option[BigDecimal]] = refs.get.map { case (lastBlock, balances, memPool) => balances.get(address) }

  def lastValidBlock(): F[Block] = refs.get.map { case (lastBlock, balances, memPool) => lastBlock }

object UnichainService {

  def apply[F[_] : Async](xa: Transactor[F]): F[UnichainService[F]] = {
    val ledgerDb = LedgerDB(xa)
    for {
      lastBlock <- ledgerDb.getLastBlock
      balances <- buildBalances(ledgerDb.getTransactions)
      refs <- Ref.of((lastBlock, balances, Seq.empty[Transaction]))
    } yield new UnichainService[F](nodeConfig)(refs, ledgerDb)
  }

  def buildBalances[F[_] : Concurrent](txs: fs2.Stream[F, Transaction]): F[Map[Address, BigDecimal]] =
    txs.compile.fold(Map.empty[Address, BigDecimal]) { case (m, tx) =>
      val k = tx.destination
      val currentVal = m.getOrElse(tx.destination, BigDecimal(0))
      m + (tx.destination -> (currentVal + tx.amount))
    }
}
