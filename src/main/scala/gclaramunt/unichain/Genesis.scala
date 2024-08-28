package gclaramunt.unichain

import cats.effect.{IO, IOApp}
import doobie.hikari.HikariTransactor.fromHikariConfig
import gclaramunt.unichain.Config.nodeConfig
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, pubKeyToAddress, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import gclaramunt.unichain.blockchain.{Block, BlockchainOps, Transaction}
import gclaramunt.unichain.store.LedgerDB

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
          _ <- ledgerDb.addBlock(genesisBlock)
          _ <- ledgerDb.addTransaction(newId, treasuryTx)
        } yield ()
      }
