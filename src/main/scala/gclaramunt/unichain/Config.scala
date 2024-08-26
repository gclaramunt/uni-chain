package gclaramunt.unichain

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo

import java.security.PrivateKey
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter

import java.io.StringReader

object Config:

  val transactionsPerBlock = 10
  
  val privateKey = loadPrivateKeyFromEnv("PRIVATE_KEY")

  def loadPrivateKeyFromEnv(envVarName: String): PrivateKey =
    val pemKey = System.getenv(envVarName)
    
    if (pemKey == null)
      throw new IllegalArgumentException(s"Environment variable $envVarName not found")

    val parser = new PEMParser(new StringReader(pemKey))
    val obj = parser.readObject()
    parser.close()

    obj match 
      case privateKeyInfo: PrivateKeyInfo =>
        val converter = new JcaPEMKeyConverter().setProvider("BC")
        converter.getPrivateKey(privateKeyInfo)
      case _ => throw new IllegalArgumentException("Invalid private key format")
