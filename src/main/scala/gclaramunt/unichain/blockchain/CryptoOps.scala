package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.Hash
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.{PEMKeyPair, PEMParser}
import org.bouncycastle.util.encoders.Hex

import java.io.StringReader
import java.security.{PrivateKey, PublicKey, Security, Signature}
import scala.util.Try

object CryptoOps:
  // Add Bouncy Castle as a security provider
  Security.addProvider(new BouncyCastleProvider())
  
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


  def loadKeysFromEnv (envVariable: String): (PrivateKey, PublicKey)=
    val pemKey = sys.env.getOrElse(envVariable, throw new IllegalArgumentException(s"Environment variable $envVariable is not set or is empty."))
    decodePEMKeys(pemKey)
  
  def decodePEMKeys(pemKey: String): (PrivateKey, PublicKey) = 
    val pemParser = new PEMParser(new StringReader(pemKey))
    val pemObject = pemParser.readObject()
    pemParser.close()

    val converter = new JcaPEMKeyConverter().setProvider("BC")
    pemObject match 
      case keyPair: PEMKeyPair => 
        (converter.getPrivateKey(keyPair.getPrivateKeyInfo), 
        converter.getPublicKey(keyPair.getPublicKeyInfo))
      case _ => throw new IllegalArgumentException("Unsupported key format")

  
  


