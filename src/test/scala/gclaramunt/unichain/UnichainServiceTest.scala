package gclaramunt.unichain

import cats.effect.IO
import gclaramunt.unichain.Config.{CryptoConfig, DbConfig, NodeConfig}
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, Transaction}
import munit.{CatsEffectSuite, FunSuite}

class UnichainServiceTest extends CatsEffectSuite:
//  class UnichainService[F[_] : MonadCancelThrow](config: NodeConfig)(refs: Ref[F, (Block, Map[Address, BigDecimal], Seq[Transaction])], ledgerDB: LedgerDB[F]):
//    private val bOps = BlockchainOps(config.crypto)
//    def submitTx(tx: Transaction): F[Unit] =
//  def addressBalance(address: Address): F[Option[BigDecimal]] =
  //    refs.get.map:
  //      case (lastBlock, balances, memPool) => balances.get(address)
  //
  //  def lastValidBlock(): F[Block] =
  private val config = NodeConfig(DbConfig("","",None,None,1), CryptoConfig(serverPrvKeyStr), 5L)
  
  def buildSvc(initialBlock: Block, balances: Map[Address, BigDecimal], currrentTxs: Seq[Transaction]): UnichainService[IO] 


  test("Submit a transaction updates balance"):
    assertIO(IO(false), true)

  test("Submit transactions updates balance and emit block "):
    assertIO(IO(false), true)

  test("Get balance for an address"):
    assertIO(IO(false), true)

  test("Get latest block"):
    assertIO(IO(false), true)