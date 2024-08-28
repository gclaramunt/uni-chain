package gclaramunt.unichain

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
  test("Submit a transaction updates balance"):
    assertEquals(false, true)

  test("Submit atransactions updates balance and emit block "):
    assertEquals(false, true)

  test("Get balance for an address"):
    assertEquals(false, true)

  test("Get latest block"):
    assertEquals(false, true)