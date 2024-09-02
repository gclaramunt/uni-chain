package gclaramunt.unichain.blockchain

import gclaramunt.unichain.*
import gclaramunt.unichain.UnichainError.toTry
import gclaramunt.unichain.blockchain.BlockchainOps.{blockHash, buildTx}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, pubKeyToAddress, sign, validate}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash}
import munit.FunSuite



class BlockchainOpsTest extends FunSuite:

  val bo = new BlockchainOps(serverPrvKeyStr)

  test("newBlock with transactions"):
    (for 
      prevHash <- BlockchainOps.blockHash(1, Seq())
      prevSig <- sign(Hash.value(prevHash), bo.privateKey)
      previous = Block(1, prevHash, Hash.from(Array.empty[Byte]), prevSig)

      serverAddress = pubKeyToAddress(bo.publicKey)
      tx1 <- buildTx(serverAddress, Address("12345"), BigDecimal(10.60), 1, bo.privateKey)
      tx2 <- buildTx(pubKeyToAddress(w1PubKey), serverAddress, BigDecimal(12.20), 2, w1PrvKey)
      tx3 <- buildTx(serverAddress, Address("45789"), BigDecimal(25.30), 3, bo.privateKey)

      txs = Seq(tx1, tx2, tx3)

      newBlockHash <- blockHash(2, txs)
      hashToSign <- hash(Hash.value(prevHash) ++ Hash.value(newBlockHash))
      hashBytes = Hash.value(hashToSign)
      signed <- sign(hashBytes, bo.privateKey)
      newBlock <- toTry(bo.newBlock(previous, txs))
      validated <- validate(hashBytes, newBlock.signature, bo.publicKey)
    yield 
      assertEquals(newBlock.id, 2L)
      assertEquals(Hash.value(newBlock.previousHash).toSeq, Hash.value(prevHash).toSeq)
      assertEquals(validated, true)
    ).get

  test("validate transaction"):
    val isValid = for 
      tx <- buildTx(pubKeyToAddress(w1PubKey), Address("doesn't matter"), BigDecimal(10.00),1, w1PrvKey)
      isValid <-BlockchainOps.validate(tx)
    yield isValid
    assertEquals(isValid.get, true)

  test("not validate transaction with invalid source"):
    val isValid = for
      tx <- buildTx(pubKeyToAddress(bo.publicKey), Address("doesn't matter"), BigDecimal(10.00), 1, w1PrvKey)
      isValid <- BlockchainOps.validate(tx)
    yield isValid
    assertEquals(isValid.get, false)



