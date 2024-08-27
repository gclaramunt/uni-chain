package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.BlockchainOps.blockHash
import gclaramunt.unichain.blockchain.CryptoOps.{hash, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Hash, Sig}
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
  
  //def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block
  // def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block =
  //    val newId = prevBlock.id+1
  //    val prevBlockHash = blockHash(prevBlock.id, prevBlock.txs)
  //    val newBlockHash = blockHash(newId, memPool)
  //    val hashData = Hash.value(prevBlockHash) ++ Hash.value(newBlockHash)
  //    val signature = Sig(sign(hashData, privateKey))
  //    Block(newId, memPool, prevBlockHash, signature)

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
  test("validate transaction"):
    val tx = Transaction()
      assertEquals(validate(hashToSign, newBlock.signature, bo.publicKey), Try {
        true
      })

  // def addressToPubKey(address: Address): PublicKey =


