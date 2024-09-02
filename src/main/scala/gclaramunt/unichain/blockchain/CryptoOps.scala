package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.{PEMKeyPair, PEMParser}
import org.bouncycastle.util.encoders.Base64

import java.io.StringReader
import java.security.*
import java.security.spec.X509EncodedKeySpec
import scala.util.{Failure, Try}

object CryptoOps:
  
  // Add Bouncy Castle as a security provider
  Security.addProvider(new BouncyCastleProvider())
  
  def hash(input: Array[Byte]): Try[Hash] =
    Try:
      val digest = new DigestSHA3(256)
      val hashBytes = digest.digest(input)
      Hash.from(hashBytes)

  def sign(data: Array[Byte], privateKey: PrivateKey): Try[Sig] =
    Try:
      val signature = Signature.getInstance("SHA256withECDSA", "BC")
      signature.initSign(privateKey)
      signature.update(data)
      Sig(signature.sign)

  def validate(data: Array[Byte], signed: Sig, pubKey: PublicKey): Try[Boolean] =
    Try:
      val signature = Signature.getInstance("SHA256withECDSA", "BC")
      signature.initVerify(pubKey)
      signature.update(data)
      signature.verify(Sig.value(signed)) 
  
  def loadKeysFromEnv (envVariable: String): Try[(PrivateKey, PublicKey)]=
    sys.env.get(envVariable).fold(
        Failure(new IllegalArgumentException(s"Environment variable $envVariable is not set or is empty."))
    )(Try.apply).flatMap(pemKey => decodePEMKeys(pemKey))
  
  def decodePEMKeys(pemKey: String): Try[(PrivateKey, PublicKey)] =
    Try:  
      val pemParser = new PEMParser(new StringReader(pemKey))
      val pemObject = pemParser.readObject()
      pemParser.close()

      val converter = new JcaPEMKeyConverter().setProvider("BC")
      pemObject match 
        case keyPair: PEMKeyPair => 
          (converter.getPrivateKey(keyPair.getPrivateKeyInfo), 
          converter.getPublicKey(keyPair.getPublicKeyInfo))
        case _ => throw new IllegalArgumentException("Unsupported key format")

  def addressToPubKey(publicKeyStr: Address): Try[PublicKey] =
    Try:
      val decodedBytes = Base64.decode(Address.value(publicKeyStr))
  
      // Convert the bytes back to a PublicKey object
      val keySpec = new X509EncodedKeySpec(decodedBytes)
      val keyFactory = KeyFactory.getInstance("EC")
      keyFactory.generatePublic(keySpec)

  def pubKeyToAddress(pubKey: PublicKey): Address =
     // Encode the bytes to a Base64 string
     Address(Base64.toBase64String(pubKey.getEncoded))


