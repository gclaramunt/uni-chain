package gclaramunt.unichain

import cats.effect.IO
import gclaramunt.unichain.Config.{CryptoConfig, DbConfig, NodeConfig, ServerConfig}
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildBlock, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.pubKeyToAddress
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, Transaction}
import gclaramunt.unichain.store.LedgerDB
import munit.CatsEffectSuite

import java.security.PrivateKey

class UnichainServiceTest extends CatsEffectSuite:

  private def ledgerDb(lastBlock: Block, txs: Seq[Transaction])= new LedgerDB[IO]:
    override def getLastBlock: IO[Block] = IO(lastBlock)
    override def getTransactions: fs2.Stream[IO, Transaction] = fs2.Stream.emits(txs)
    override def addBlock(b: Block): IO[Int] = IO(1)
    override def addTransaction(blockId: Long, tx: Transaction): IO[Int] = IO(1)
    override def addBlockWithTransactions(b: Block, txs: Seq[Transaction]): IO[Unit] = IO.unit

  private val testConfig = NodeConfig(
    db = DbConfig("org.h2.Driver", "jdbc:h2:mem:test", None, None, 1),
    crypto = CryptoConfig(serverPrvKeyStr),
    server = ServerConfig("localhost", 9999),
    transactionsPerBlock = 3
  )

  private def buildSvc(ledgerDbMock: LedgerDB[IO]): IO[UnichainService[IO]] =
    UnichainService(ledgerDbMock, testConfig)


  private val block = buildBlock(1, Seq(), blockHash(1, Seq()).get, serverPrvKey).get
  private val serverAdd = pubKeyToAddress(serverPubKey)
  private val w1Add = pubKeyToAddress(w1PubKey)
  private val w2Add = pubKeyToAddress(w2PubKey)

  private val currentTxs = Seq(
    buildTx(serverAdd, serverAdd, BigDecimal(80), 0, serverPrvKey),
    buildTx(serverAdd, w1Add, BigDecimal(40), 1, serverPrvKey),
    buildTx(w1Add, w2Add, BigDecimal(20), 0, w1PrvKey),
    buildTx(w2Add, w1Add, BigDecimal(10), 0, w2PrvKey),
  ).map(_.get)

  def buildSubmitTx(svc: UnichainService[IO], source: Address, dest: Address, amount: BigDecimal, nonce: Long, privateKey: PrivateKey): IO[Unit] =
    for
      newTx <- IO.fromTry(buildTx(source, dest, amount, nonce, privateKey))
      _ <- svc.submitTx(newTx)
    yield ()

  test("Submit a transaction updates balance"):
    val exec = for
      svc <-buildSvc(ledgerDb(block, currentTxs))
      _ <- buildSubmitTx(svc, w2Add, w1Add, BigDecimal(5), 1, w2PrvKey)
      balance <- svc.addressBalance(w1Add)
    yield balance
    assertIO(exec, Some(BigDecimal(35)))

  test("Submit transactions updates balance and emit block "):
    val svcF = buildSvc(ledgerDb(block, currentTxs))
    val exec = for
      svc <-svcF
      _ <- buildSubmitTx(svc, w2Add, w1Add, BigDecimal(5), 1, w2PrvKey)
      _ <- buildSubmitTx(svc, w1Add, w2Add, BigDecimal(5), 1, w1PrvKey)
      _ <- buildSubmitTx(svc, w2Add, w1Add, BigDecimal(5), 2, w2PrvKey)
      _ <- buildSubmitTx(svc, w1Add, w2Add, BigDecimal(5), 2, w1PrvKey)
      balance <- svc.addressBalance(w1Add)
      newBlock <- svc.lastValidBlock()
    yield (balance,newBlock.id)
    assertIO(exec, (Some(BigDecimal(30)), 2L))

  test("Submit a transaction exceeding balance"):
    val exec = for
      svc <- buildSvc(ledgerDb(block, currentTxs))
      _ <- buildSubmitTx(svc, w2Add, w1Add, BigDecimal(500), 1, w2PrvKey)
      balance <- svc.addressBalance(w1Add)
    yield balance

    interceptMessageIO[RuntimeException]("Source final balance can't be less than 0")(exec)

  test("Submit a transaction with invalid nonce"):
    val exec = for
      svc <- buildSvc(ledgerDb(block, currentTxs))
      _ <- buildSubmitTx(svc, w2Add, w1Add, BigDecimal(5), 0, w2PrvKey)
    yield ()

    interceptMessageIO[RuntimeException]("Invalid nonce: 0, expected > 0")(exec)

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
