package gclaramunt.unichain.blockchain

import gclaramunt.unichain.*
import gclaramunt.unichain.blockchain.CryptoOps.*
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import munit.FunSuite
import org.bouncycastle.util.encoders.Hex

class CryptoOpsTest extends FunSuite:
  
  test("Hash a string"):
    val hashValue = Hash.value(hash("Hello world".getBytes).get)
    assertEquals(Hex.toHexString(hashValue), "369183d3786773cef4e56c7b849e7ef5f742867510b676d6b38f8e38a222d8a2")

  test("validate signed data"):
    val data = "Hello world!".getBytes
    val signed = sign(data, serverPrvKey).get
    val validated = validate(data, signed, serverPubKey).get
    assertEquals(validated, true )

  test("don't validate tampered data"):
    val data = "Hello world!".getBytes
    val signed = sign(data, serverPrvKey).get
    val validated = validate("Goodbye world!".getBytes, signed, serverPubKey).get
    assertEquals(validated, false )
