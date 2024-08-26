package gclaramunt.unichain

import cats.effect.kernel.Concurrent
import cats.effect.{Async, Ref}
import cats.syntax.all.*
import doobie.Transactor
import doobie.hikari.HikariTransactor.fromHikariConfig
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, Transaction}
import gclaramunt.unichain.store.{LedgerDB, hikariConfig}

class UnichainService[F[_]](lastBlock: Ref[F, Option[Block]], ledgerStatus: Ref[F,Map[Address, BigDecimal]]):
  
  def submitTx(tx: Transaction): F[String] = ???

  def addressBalance(address: Address): F[BigDecimal] = ???
  
  def lastValidBlock(): F[Block] = ???
  
    
  //val ledgerStatus: Map[Address, BigDecimal]

object UnichainService {

  def apply[F[_]: Async](xa: Transactor[F]): F[UnichainService[F]] = {
    val ledgerDb = LedgerDB(xa)
    for {
//      xa <- fromHikariConfig[F](hikariConfig)
      lastBlock <- ledgerDb.getLastBlock()
      ledger <- buildLedger(ledgerDb.getTransactions())
      blockRef <- Ref.of(lastBlock)
      ledgerRef <- Ref.of(ledger)
    } yield new UnichainService[F](blockRef,ledgerRef)
  }

  def buildLedger[F[_]: Concurrent](txs: fs2.Stream[F, Transaction]): F[Map[Address, BigDecimal]] =
    txs.compile.fold(Map.empty[Address, BigDecimal]){case (m, tx) =>
      val k = tx.destination
      val currentVal = m.getOrElse(tx.destination, BigDecimal(0))
      m + (tx.destination -> (currentVal + tx.amount))
    }
}
