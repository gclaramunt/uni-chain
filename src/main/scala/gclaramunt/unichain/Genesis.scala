package gclaramunt.unichain

import cats.effect.{ExitCode, IO, IOApp}
import doobie.*
import doobie.hikari.HikariTransactor.fromHikariConfig
import doobie.implicits.*
import gclaramunt.unichain.Config.nodeConfig
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildBlock, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.pubKeyToAddress
import gclaramunt.unichain.blockchain.{BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB

object Genesis extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    val intialTreasury = BigDecimal(args(0))
    val bOps = BlockchainOps(nodeConfig.crypto)
    val treasuryAddress = pubKeyToAddress(bOps.publicKey)

    val treasuryTx = buildTx(treasuryAddress, treasuryAddress, intialTreasury,0, bOps.privateKey).get

    val newId = 0
    
    fromHikariConfig[IO](Config.hikariConfig).use: xa =>
        val ledgerDb = LedgerDB(xa)
        for
          genesisBlock <- IO.fromTry(for 
                            newBlockHash <- blockHash(newId, Seq(treasuryTx))
                            genesisBlock <- buildBlock(newId,Seq(treasuryTx), newBlockHash, bOps.privateKey )
                          yield genesisBlock)  
          _ <- Schema.blockTable.transact(xa)
          _ <- Schema.txTable.transact(xa)
          _ <- ledgerDb.addBlock(genesisBlock)
          _ <- ledgerDb.addTransaction(newId, treasuryTx)
        yield (ExitCode.Success)
      


object Schema:

  val blockTable: doobie.ConnectionIO[Int] =sql"""CREATE TABLE IF NOT EXISTS blocks(
    id BIGINT,
    hash VARBINARY,
    previous_hash VARBINARY,
    signature VARBINARY
  );""".update.run

  val txTable: doobie.ConnectionIO[Int] = sql"""CREATE TABLE IF NOT EXISTS transactions(
    source VARCHAR,
    destination VARCHAR,
    amount NUMERIC,
    signature VARBINARY,
    hash VARBINARY,
    nonce BIGINT,
    block_id BIGINT
  );""".update.run