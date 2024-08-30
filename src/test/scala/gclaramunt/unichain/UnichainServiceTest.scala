package gclaramunt.unichain

import cats.effect.IO
import gclaramunt.unichain.Config.{CryptoConfig, nodeConfig}
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildBlock, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{pubKeyToAddress, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash}
import gclaramunt.unichain.blockchain.{Block, Transaction}
import gclaramunt.unichain.store.LedgerDB
import munit.CatsEffectSuite

class UnichainServiceTest extends CatsEffectSuite:

  private def ledgerDb(lastBlock: Block, txs: Seq[Transaction])= new LedgerDB[IO]:
    override def getLastBlock: IO[Block] = IO(lastBlock)
    override def getTransactions: fs2.Stream[IO, Transaction] = fs2.Stream.emits(txs)
    override def addBlock(b: Block): IO[Int] = IO(1)
    override def addTransaction(blockId: Long, tx: Transaction): IO[Int] = IO(1)

  private def buildSvc(ledgerDbMock: LedgerDB[IO]): IO[UnichainService[IO]] =
    UnichainService(ledgerDbMock, nodeConfig.copy(transactionsPerBlock = 3, crypto = CryptoConfig(serverPrvKeyStr)))


  private val block = buildBlock(1, Seq(), blockHash(1, Seq()), serverPrvKey)
  private val serverAdd = pubKeyToAddress(serverPubKey)
  private val w1Add = pubKeyToAddress(w1PubKey)
  private val w2Add = pubKeyToAddress(w2PubKey)

  private val currentTxs = Seq(
    buildTx(serverAdd, serverAdd, BigDecimal(80), 0, serverPrvKey),
    buildTx(serverAdd, w1Add, BigDecimal(40), 0, serverPrvKey),
    buildTx(w1Add, w2Add, BigDecimal(20), 0, w1PrvKey),
    buildTx(w2Add, w1Add, BigDecimal(10), 0, w2PrvKey),
  )

  test("Submit a transaction updates balance"):
    val exec = for
      svc <-buildSvc(ledgerDb(block, currentTxs))
      _ <- svc.submitTx(buildTx(w2Add, w1Add, BigDecimal(5), 0, w2PrvKey))
      balance <- svc.addressBalance(w1Add)
    yield balance
    assertIO(exec, Some(BigDecimal(35)))

  test("Submit transactions updates balance and emit block "):
    val svcF = buildSvc(ledgerDb(block, currentTxs))
    val exec = for
      svc <-svcF
      _ <- svc.submitTx(buildTx(w2Add, w1Add, BigDecimal(5), 0, w2PrvKey))
      _ <- svc.submitTx(buildTx(w1Add, w2Add, BigDecimal(5), 0, w1PrvKey))
      _ <- svc.submitTx(buildTx(w2Add, w1Add, BigDecimal(5), 0, w2PrvKey))
      _ <- svc.submitTx(buildTx(w1Add, w2Add, BigDecimal(5), 0, w1PrvKey))
      balance <- svc.addressBalance(w1Add)
      newBlock <- svc.lastValidBlock()
    yield (balance,newBlock.id)
    assertIO(exec, (Some(BigDecimal(30)), 2L))

  test("Submit a transaction exceeding balance"):
    val exec = for
      svc <- buildSvc(ledgerDb(block, currentTxs))
      _ <- svc.submitTx(buildTx(w2Add, w1Add, BigDecimal(500), 0, w2PrvKey))
      balance <- svc.addressBalance(w1Add)
    yield balance

    interceptMessageIO[RuntimeException]("Source final balance can't be less than 0")(exec)


  test("Get balance for an address"):
    val exec = for
      svc <-buildSvc(ledgerDb(block, currentTxs))
      balance <- svc.addressBalance(w1Add)
    yield balance
    assertIO(exec, Some(BigDecimal(30)))

  test("Get latest block"):
    val exec = for
      svc <- buildSvc(ledgerDb(block, currentTxs))
      block <- svc.lastValidBlock()
    yield block
    assertIO(exec, block)