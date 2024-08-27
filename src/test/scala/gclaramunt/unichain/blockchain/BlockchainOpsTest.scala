package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.BlockchainOps.blockHash
import gclaramunt.unichain.blockchain.CryptoOps.{hash, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import munit.FunSuite

import scala.util.Try

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

    val newBlock = bo.newBlock(previous, Seq.empty)
    assertEquals(newBlock.id, 2L)
    assertEquals(Hash.value(newBlock.previousHash).toSeq, Hash.value(prevHash).toSeq)
    assertEquals(validate(hashToSign, newBlock.signature, bo.publicKey), Try { true})


  //def validate(tx: Transaction): Try[Boolean] =
  //def validate(tx: Transaction): Try[Boolean] =
  //    CryptoOps.validate(Hash.value(tx.hash), tx.signature, addressToPubKey(tx.source))
  //source: Address, destination: Address, amount: BigDecimal, signature: Sig, hash: Hash, nonce: Long
  test("validate transaction"):
    val source = """-----BEGIN PUBLIC KEY-----
                   |MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHSAlnIFyECxFABBpY7ZLub4udfHx
                   |CbDwvnR4RUhOqeKSo6lXMkQeH9joWEEA7kqGjCCmHaAMwfg8OAxnW/CtrA==
                   |-----END PUBLIC KEY-----
                   |""".stripMargin
    val txCore = TransactionCore(Address(source), Address("45789"), BigDecimal(10.00),1)
    val txHash = BlockchainOps.txHash(txCore)
    val sig = sign( Hash.value(txHash), bo.privateKey)

    val tx = Transaction(txCore, txHash, sig)

    assertEquals(bo.validate(tx), Try {
      true
    })

  // def addressToPubKey(address: Address): PublicKey =


