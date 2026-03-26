package gclaramunt.unichain

import cats.effect.{ExitCode, IO, IOApp}
import doobie.*
import doobie.hikari.HikariTransactor.fromHikariConfig
import doobie.implicits.*
import gclaramunt.unichain.Config.nodeConfig
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildBlock, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.pubKeyToAddress
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import gclaramunt.unichain.blockchain.{BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB

object Genesis extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    val intialTreasury = BigDecimal(args(0))
    for
      bOps <- IO.fromTry(BlockchainOps.fromConfig(nodeConfig.crypto))
      treasuryAddress = pubKeyToAddress(bOps.publicKey)
      treasuryTx <- IO.fromTry(buildTx(treasuryAddress, treasuryAddress, intialTreasury, 0, bOps.privateKey))
      emptyHash = Hash.from(Array.empty[Byte])
      genesisBlock <- IO.fromTry(for
        newBlockHash <- blockHash(0, Seq(treasuryTx))
        genesisBlock <- buildBlock(0, Seq(treasuryTx), emptyHash, bOps.privateKey)
      yield genesisBlock)
      exitCode <- fromHikariConfig[IO](Config.hikariConfig).use: xa =>
        val ledgerDb = LedgerDB(xa)
        for
          _ <- Schema.blockTable.transact(xa)
          _ <- Schema.txTable.transact(xa)
          _ <- ledgerDb.addBlock(genesisBlock)
          _ <- ledgerDb.addTransaction(0, treasuryTx)
        yield ExitCode.Success
    yield exitCode


object Schema:

  val blockTable: doobie.ConnectionIO[Int] = sql"""CREATE TABLE IF NOT EXISTS blocks(
    id BIGINT PRIMARY KEY,
    hash VARBINARY NOT NULL,
    previous_hash VARBINARY NOT NULL,
    signature VARBINARY NOT NULL
  );""".update.run

  val txTable: doobie.ConnectionIO[Int] = sql"""CREATE TABLE IF NOT EXISTS transactions(
    source VARCHAR NOT NULL,
    destination VARCHAR NOT NULL,
    amount NUMERIC NOT NULL,
    signature VARBINARY NOT NULL,
    hash VARBINARY NOT NULL,
    nonce BIGINT NOT NULL,
    block_id BIGINT NOT NULL REFERENCES blocks(id)
  );""".update.run
