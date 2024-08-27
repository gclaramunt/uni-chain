package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoOps.{decodePEMPrivateKey, hash, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import munit.FunSuite

class CryptoOpsTest extends FunSuite {

  test("Hash a string") {
    val strHash = String(Hash.value(hash("Hello world")))
    assertEquals(strHash, "369183d3786773cef4e56c7b849e7ef5f742867510b676d6b38f8e38a222d8a2")
  }

  test("Sign") {
    val data = "Hello world"
    val key =
      """
        |-----BEGIN EC PRIVATE KEY-----
        |MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
        |AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
        |ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
        |-----END EC PRIVATE KEY-----
        |""".stripMargin
    val privateKey = decodePEMPrivateKey(key)
    sign(data.getBytes, privateKey)
  }

  test("Validate") {

  }
}
