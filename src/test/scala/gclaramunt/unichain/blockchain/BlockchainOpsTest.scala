package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, pubKeyToAddress, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash}
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

  test("newBlock without transactions"):
    val prevHash = BlockchainOps.blockHash(1, Seq())
    val prevSig = sign(Hash.value(prevHash), bo.privateKey)
    val previous = Block(1,Seq(),Hash(Array.empty[Byte]), prevSig)

    val newBlockHash = blockHash(2, Seq())
    val hashToSign = Hash.value(hash(Hash.value(prevHash) ++ Hash.value(newBlockHash)))
    val signed =sign( hashToSign, bo.privateKey)

    val newBlock = bo.newBlock(previous, Seq.empty).get
    assertEquals(newBlock.id, 2L)
    assertEquals(Hash.value(newBlock.previousHash).toSeq, Hash.value(prevHash).toSeq)
    assertEquals(validate(hashToSign, newBlock.signature, bo.publicKey).get, true)


  test("validate transaction"):
    val source = pubKeyToAddress(bo.publicKey)
    val tx = buildTx(source, Address("45789"), BigDecimal(10.00),1, bo.privateKey)
    assertEquals(bo.validate(tx).get, true)



