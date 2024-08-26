package gclaramunt.unichain

import cats.{Monad, MonadError}
import cats.effect.kernel.{Concurrent, MonadCancelThrow}
import cats.effect.std.MapRef
import cats.effect.{Async, MonadCancel, Ref}
import cats.syntax.all.*
import doobie.Transactor
import doobie.hikari.HikariTransactor.fromHikariConfig
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, BlockchainOps, Transaction}
import gclaramunt.unichain.store.{LedgerDB, hikariConfig}

import java.security.PublicKey


// TODO Treasury / genesis block
class UnichainService[F[_] : MonadCancelThrow](refs: Ref[F, (Block, Map[Address, BigDecimal], Seq[Transaction])], ledgerDB: LedgerDB[F]):
  def submitTx(tx: Transaction): F[Unit] =
    for {
      isValid <- MonadCancelThrow[F].fromTry(BlockchainOps.validate(tx))
      // TODO vallidate TX signature
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
            if (updatedMemPool.size < Config.TransactionsPerBlock) {
              ((lastBlock, updatedBalances, updatedMemPool), ledgerDB.addTransaction(lastBlock.id, updatedMemPool).map(_ => ()))
            } else {
              val newBlock = newBlock(lastBlock, updatedMemPool)
              val dbUpdate = for {
                _ <- ledgerDB.addBlock(newBlock)
                _ <- ledgerDB.addTransaction(newBlock, updatedMemPool)
              } yield ()
              ((newBlock, updatedBalances, Seq()), dbUpdate)
            }
          } else ((lastBlock, balances, memPool), MonadCancelThrow[F].raiseError(new RuntimeException("Source final balance can't be less than 0")))
        }
    } yield ()


  def addressBalance(address: Address): F[Option[BigDecimal]] = refs.get.map { case (lastBlock, balances, memPool) => balances.get(address) }

  def lastValidBlock(): F[Block] = refs.get.map { case (lastBlock, balances, memPool) => lastBlock }


//val ledgerStatus: Map[Address, BigDecimal]

object UnichainService {

  def apply[F[_] : Async](xa: Transactor[F]): F[UnichainService[F]] = {
    val ledgerDb = LedgerDB(xa)
    for {
      //      xa <- fromHikariConfig[F](hikariConfig)
      lastBlock <- ledgerDb.getLastBlock
      balances <- buildBalances(ledgerDb.getTransactions)
      refs <- Ref.of((lastBlock, balances, Seq.empty[Transaction]))
    } yield new UnichainService[F](refs, ledgerDb)
  }

  def buildBalances[F[_] : Concurrent](txs: fs2.Stream[F, Transaction]): F[Map[Address, BigDecimal]] =
    txs.compile.fold(Map.empty[Address, BigDecimal]) { case (m, tx) =>
      val k = tx.destination
      val currentVal = m.getOrElse(tx.destination, BigDecimal(0))
      m + (tx.destination -> (currentVal + tx.amount))
    }
}
