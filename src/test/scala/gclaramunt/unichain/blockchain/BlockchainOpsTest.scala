package gclaramunt.unichain.blockchain

import gclaramunt.unichain.*
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, pubKeyToAddress, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash}
import munit.FunSuite

class BlockchainOpsTest extends FunSuite:

  val bo = new BlockchainOps(serverPrvKeyStr)

  test("newBlock with transactions"):
    val prevHash = BlockchainOps.blockHash(1, Seq())
    val prevSig = sign(Hash.value(prevHash), bo.privateKey)
    val previous = Block(1, prevHash, Hash.from(Array.empty[Byte]), prevSig)

    val serverAddress = pubKeyToAddress(bo.publicKey)
    val tx1 = buildTx(serverAddress, Address("12345"), BigDecimal(10.60), 1, bo.privateKey)
    val tx2 = buildTx(pubKeyToAddress(w1PubKey), serverAddress, BigDecimal(12.20), 2, w1PrvKey)
    val tx3 = buildTx(serverAddress, Address("45789"), BigDecimal(25.30), 3, bo.privateKey)

    val txs = Seq(tx1,tx2,tx3)

    val newBlockHash = blockHash(2, txs)
    val hashToSign = Hash.value(hash(Hash.value(prevHash) ++ Hash.value(newBlockHash)))
    val signed = sign(hashToSign, bo.privateKey)

    val newBlock = bo.newBlock(previous, txs).toOption.get
    assertEquals(newBlock.id, 2L)
    assertEquals(Hash.value(newBlock.previousHash).toSeq, Hash.value(prevHash).toSeq)
    assertEquals(validate(hashToSign, newBlock.signature, bo.publicKey), true)

  test("validate transaction"):
    val tx = buildTx(pubKeyToAddress(w1PubKey), Address("doesn't matter"), BigDecimal(10.00),1, w1PrvKey)
    assertEquals(bo.validate(tx), true)

  test("not validate transaction with invalid source"):
    val tx = buildTx(pubKeyToAddress(bo.publicKey), Address("doesn't matter"), BigDecimal(10.00), 1, w1PrvKey)
    assertEquals(bo.validate(tx), false)



