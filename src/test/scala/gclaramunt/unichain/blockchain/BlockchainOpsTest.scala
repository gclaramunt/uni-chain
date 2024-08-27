package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.{Hash, Sig}
import munit.FunSuite

class BlockchainOpsTest extends FunSuite:

  private val prvKeyStr =
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
      |AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
      |ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin
    
  val bo = new BlockchainOps(prvKeyStr)
  
  //def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block
  test("newBlock"):
    val previous = Block(1,Seq(),Hash(Array.empty[Byte]), Sig(Array.empty[Byte]))
    val newBlock = bo.newBlock(previous, Seq.empty)
    assertEquals(newBlock, previous)

  //def validate(tx: Transaction): Try[Boolean] =

  // def addressToPubKey(address: Address): PublicKey =


