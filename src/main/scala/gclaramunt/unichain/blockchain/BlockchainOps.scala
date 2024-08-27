package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.Config.CryptoConfig
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.CryptoOps.{decodePEMKeys, hash, sign}

import java.security.{KeyFactory, PublicKey}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import scala.util.Try

class BlockchainOps(pemKey: String):
  
  val (privateKey, publicKey) = decodePEMKeys(pemKey)
  
  def blockHash(block: Block): Hash =
    hash(s"${block.id}${block.txs.map(_.hash)}")

  def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block =
    val prevBlockHash = blockHash(prevBlock)
    val newBlockHash = blockHash(prevBlock)
    val hash = prevBlockHash + newBlockHash
    val signature = Sig(sign(Hash.value(hash), privateKey))
    Block(prevBlock.id+1, memPool, prevBlockHash, signature)

  def validate(tx: Transaction): Try[Boolean] =
    CryptoOps.validate(Hash.value(tx.hash), Sig.value(tx.signature), addressToPubKey(tx.source))


  def addressToPubKey(address: Address): PublicKey =
    val keyBytes = Base64.getDecoder.decode(Address.value(address))
    val keySpec = new X509EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("EC", "BC")
    keyFactory.generatePublic(keySpec)
    
    
object BlockchainOps:
  def apply(cfg: CryptoConfig) = new BlockchainOps(cfg.privateKey)