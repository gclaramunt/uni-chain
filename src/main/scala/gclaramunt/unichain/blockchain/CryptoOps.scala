package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.{Hash, Sig}
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo

import java.security.{KeyFactory, KeyPairGenerator, KeyStore, PrivateKey, PublicKey, Security, Signature}
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

import java.io.StringReader
import java.io.FileInputStream
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import scala.util.Try

object CryptoOps:

  // Add Bouncy Castle as a security provider
  Security.addProvider(new BouncyCastleProvider())

//  def generateKeyPair(): (PrivateKey, java.security.PublicKey) = {
//    val keyGen = KeyPairGenerator.getInstance("RSA", "BC")
//    keyGen.initialize(2048)
//    val keyPair = keyGen.generateKeyPair()
//    (keyPair.getPrivate, keyPair.getPublic)
//  }

  def hash(input: String): Hash =
    val digest = new DigestSHA3(256)
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    Hash(Hex.toHexString(hashBytes))

  def sign(data: Array[Byte], privateKey: PrivateKey): Array[Byte] =
    val signature = Signature.getInstance("SHA256withECDSA", "BC")
    signature.initSign(privateKey)
    signature.update(data)
    signature.sign

  def validate(data: Array[Byte], signed: Array[Byte], pubKey: PublicKey): Try[Boolean] =
    val signature = Signature.getInstance("SHA256withECDSA", "BC")
    signature.initVerify(pubKey)
    signature.update(data)
    Try { signature.verify(signed) }


  def loadPrivateKeyFromEnv (envVariable: String): PrivateKey=
    val pemKey = sys.env.getOrElse(envVariable, throw new IllegalArgumentException(s"Environment variable $envVariable is not set or is empty."))

    // Strip the PEM headers and footers
    val privateKeyPEM = pemKey.replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replaceAll("\\s+", "")

    // Decode the base64 string to get the binary DER format
    val keyBytes = Base64.getDecoder.decode(privateKeyPEM)

    // Convert the key bytes into a PrivateKey object
    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("EC", "BC")
    keyFactory.generatePrivate(keySpec)
  
  lazy val privateKey: PrivateKey = loadPrivateKeyFromEnv("PRIVATE_KEY")


