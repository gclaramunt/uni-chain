package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex

import java.io.FileInputStream
import java.security.{KeyPairGenerator, KeyStore, PrivateKey, Security, Signature}

object CryptoOps {

  val keystorePassword = System.getenv("KEYSTORE_PASSWORD")

  def hash(input: String): Hash = 
    val digest = new DigestSHA3(256)
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    Hash(Hex.toHexString(hashBytes))


  // Add Bouncy Castle as a security provider
  Security.addProvider(new BouncyCastleProvider())

//  def generateKeyPair(): (PrivateKey, java.security.PublicKey) = {
//    val keyGen = KeyPairGenerator.getInstance("RSA", "BC")
//    keyGen.initialize(2048)
//    val keyPair = keyGen.generateKeyPair()
//    (keyPair.getPrivate, keyPair.getPublic)
//  }

  def sign(data: Array[Byte], privateKey: PrivateKey): Array[Byte] = 
    val signature = Signature.getInstance("SHA256withRSA", "BC")
    signature.initSign(privateKey)
    signature.update(data)
    signature.sign()


  println(s"Signed hash: ${Hex.toHexString(signedHash)}")

  def loadPrivateKey(keystorePath: String, keystorePassword: String, keyAlias: String, keyPassword: String): PrivateKey = {
    val keystore = KeyStore.getInstance("PKCS12")
    val keystoreInputStream = new FileInputStream(keystorePath)
    keystore.load(keystoreInputStream, keystorePassword.toCharArray)
    keystoreInputStream.close()

    keystore.getKey(keyAlias, keyPassword.toCharArray).asInstanceOf[PrivateKey]
  }

  // Example usage
  val keystorePath = "/path/to/your/keystore.p12"
  val keyAlias = "myKeyAlias"
  val keyPassword = "keyPassword"

  val privateKey = loadPrivateKey(keystorePath, keystorePassword, keyAlias, keyPassword)

  // Assume we have a hash from a previous step
  val hashToSign = Hex.decode("5f61c92b675b1a5c82a34cddad910b15d749867ef539d56d0dedb608789c0967")

  val signedHash = sign(hashToSign, privateKey)
  println(s"Signed hash: ${Hex.toHexString(signedHash)}")

}
