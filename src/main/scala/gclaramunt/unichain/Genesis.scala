package gclaramunt.unichain

import cats.effect.{IO, IOApp}
import doobie.hikari.HikariTransactor.fromHikariConfig
import doobie.implicits.toSqlInterpolator
import gclaramunt.unichain.Config.nodeConfig
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, pubKeyToAddress, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import gclaramunt.unichain.blockchain.{Block, BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB
import doobie._
import doobie.implicits._

object Genesis extends IOApp.Simple:

  val run =
    val intialTreasury = BigDecimal(10000)
    val bOps = BlockchainOps(nodeConfig.crypto)
    val treasuryAddress = pubKeyToAddress(bOps.publicKey)

    val treasuryTx = buildTx(treasuryAddress, treasuryAddress, intialTreasury,0, bOps.privateKey)

    val newId = 0
    val newBlockHash = blockHash(newId, Seq(treasuryTx))
    val hashData = Hash.value(newBlockHash)
    val signature = sign(Hash.value(hash(hashData)), bOps.privateKey)
    val genesisBlock = Block(newId, Seq(treasuryTx), newBlockHash, signature)
    fromHikariConfig[IO](Config.hikariConfig).use { xa =>
        val ledgerDb = LedgerDB(xa)
        for {
          _ <- Schema.blockTable.transact(xa)
          _ <- Schema.txTable.transact(xa)
          _ <- ledgerDb.addBlock(genesisBlock)
          _ <- ledgerDb.addTransaction(newId, treasuryTx)
        } yield ()
      }


object Schema:

  val blockTable: doobie.ConnectionIO[Int] =sql"""CREATE TABLE IF NOT EXISTS blocks(
    id BIGINT,
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