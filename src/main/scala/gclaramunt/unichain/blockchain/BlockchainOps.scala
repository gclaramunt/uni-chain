package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.Config.CryptoConfig
import gclaramunt.unichain.blockchain.BlockchainOps.blockHash
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig, longToBytes}
import gclaramunt.unichain.blockchain.CryptoOps.{addressToPubKey, decodePEMKeys, hash, sign}

import java.security.{KeyFactory, PublicKey}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import scala.util.Try

class BlockchainOps(pemKey: String):
  
  val (privateKey, publicKey) = decodePEMKeys(pemKey)

  def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block =
    
    //TODO validate previous block
    
    val newId = prevBlock.id+1
    val prevBlockHash = blockHash(prevBlock.id, prevBlock.txs)
    val newBlockHash = blockHash(newId, memPool)
    val hashData = Hash.value(prevBlockHash) ++ Hash.value(newBlockHash)
    val signature = sign(Hash.value(hash(hashData)), privateKey)
    Block(newId, memPool, prevBlockHash, signature)

  def validate(tx: Transaction): Try[Boolean] =
    CryptoOps.validate(Hash.value(tx.hash), tx.signature, addressToPubKey(Address.value(tx.source)))

    
object BlockchainOps:

  def apply(cfg: CryptoConfig) = new BlockchainOps(cfg.privateKey)

  def blockHash(id: Long, txs: Seq[Transaction]): Hash =
    Hash(longToBytes(id) ++ txs.flatMap(t => Hash.value(t.hash)))

  def txHash(txCore: TransactionCore): Hash =
    Hash(Address.value(txCore.source).getBytes ++ Address.value(txCore.destination).getBytes ++ txCore.amount.toString().getBytes ++  longToBytes(txCore.nonce))