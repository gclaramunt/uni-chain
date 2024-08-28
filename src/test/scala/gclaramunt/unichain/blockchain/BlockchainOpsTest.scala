package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{decodePEMKeys, hash, pubKeyToAddress, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash}
import munit.FunSuite

class BlockchainOpsTest extends FunSuite:

  private val serverPrvKeyStr =
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
      |AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
      |ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin

  private val (w1PrvKey,w1PubKey) = decodePEMKeys(
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEII5rs4/l8RO9+PlTRdS/tYOnCbvwTfZYAiveCeZJ/D0boAoGCCqGSM49
      |AwEHoUQDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2
      |bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin)

  val bo = new BlockchainOps(serverPrvKeyStr)

  test("newBlock with transactions"):
    val prevHash = BlockchainOps.blockHash(1, Seq())
    val prevSig = sign(Hash.value(prevHash), bo.privateKey)
    val previous = Block(1, Seq(), Hash.from(Array.empty[Byte]), prevSig)

    val serverAddress = pubKeyToAddress(bo.publicKey)
    val tx1 = buildTx(serverAddress, Address("12345"), BigDecimal(10.60), 1, bo.privateKey)
    val tx2 = buildTx(pubKeyToAddress(w1PubKey), serverAddress, BigDecimal(12.20), 2, w1PrvKey)
    val tx3 = buildTx(serverAddress, Address("45789"), BigDecimal(25.30), 3, bo.privateKey)

    val txs = Seq(tx1,tx2,tx3)

    val newBlockHash = blockHash(2, txs)
    val hashToSign = Hash.value(hash(Hash.value(prevHash) ++ Hash.value(newBlockHash)))
    val signed = sign(hashToSign, bo.privateKey)

    val newBlock = bo.newBlock(previous, txs).get
    assertEquals(newBlock.id, 2L)
    assertEquals(Hash.value(newBlock.previousHash).toSeq, Hash.value(prevHash).toSeq)
    assertEquals(validate(hashToSign, newBlock.signature, bo.publicKey).get, true)

  test("validate transaction"):
    val tx = buildTx(pubKeyToAddress(w1PubKey), Address("doesn't matter"), BigDecimal(10.00),1, w1PrvKey)
    assertEquals(bo.validate(tx).get, true)

  test("not validate transaction with invalid source"):
    val tx = buildTx(pubKeyToAddress(bo.publicKey), Address("doesn't matter"), BigDecimal(10.00), 1, w1PrvKey)
    assertEquals(bo.validate(tx).get, false)



