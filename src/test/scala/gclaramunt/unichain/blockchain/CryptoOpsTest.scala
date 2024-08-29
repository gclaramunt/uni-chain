package gclaramunt.unichain.blockchain

import gclaramunt.unichain.*
import gclaramunt.unichain.blockchain.CryptoOps.*
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import munit.FunSuite
import org.bouncycastle.util.encoders.Hex

import scala.util.Try

class CryptoOpsTest extends FunSuite:
  
  test("Hash a string"):
    val hashValue = Hash.value(hash("Hello world".getBytes))
    assertEquals(Hex.toHexString(hashValue), "369183d3786773cef4e56c7b849e7ef5f742867510b676d6b38f8e38a222d8a2")

  test("validate signed data"):
    val data = "Hello world!".getBytes
    val signed = sign(data, serverPrvKey)
    val validated = validate(data, signed, serverPubKey)
    assertEquals(validated, true )

  test("don't validate tampered data"):
    val data = "Hello world!".getBytes
    val signed = sign(data, serverPrvKey)
    val validated = validate("Goodbye world!".getBytes, signed, serverPubKey)
    assertEquals(validated, false )
