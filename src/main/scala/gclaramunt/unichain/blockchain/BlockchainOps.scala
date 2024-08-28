package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.Config.CryptoConfig
import gclaramunt.unichain.blockchain.BlockchainOps.blockHash
import gclaramunt.unichain.blockchain.CryptoOps.{addressToPubKey, decodePEMKeys, hash, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, longToBytes}

import java.security.{PrivateKey, PublicKey}
import scala.util.Try

class BlockchainOps(pemKey: String):
  
  val (privateKey, publicKey) = decodePEMKeys(pemKey)

  def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Try[Block] =
    val prevBlockHash = blockHash(prevBlock.id, prevBlock.txs)
    CryptoOps.validate(Hash.value(prevBlockHash), prevBlock.signature, publicKey).map: 
      prevBlockValid =>
        if (prevBlockValid) then
          val newId = prevBlock.id + 1
          val newBlockHash = blockHash(newId, memPool)
          val hashData = Hash.value(prevBlockHash) ++ Hash.value(newBlockHash)
          val signature = sign(Hash.value(hash(hashData)), privateKey)
          Block(newId, memPool, prevBlockHash, signature)
        else
          throw new RuntimeException("Invalid current block")

  def validate(tx: Transaction): Try[Boolean] =
    CryptoOps.validate(Hash.value(tx.hash), tx.signature, addressToPubKey(tx.source))

    
object BlockchainOps:

  def apply(cfg: CryptoConfig) = new BlockchainOps(cfg.privateKey)

  def blockHash(id: Long, txs: Seq[Transaction]): Hash =
    hash(longToBytes(id) ++ txs.flatMap(t => Hash.value(t.hash)))

  def transactionHash(destination: Address, amount: BigDecimal, nonce: Long): Hash =
    hash(Address.value(destination).getBytes ++ amount.toString().getBytes ++  longToBytes(nonce))

  def buildTx(source: Address, dest: Address, amount: BigDecimal, nonce: Long, privateKey: PrivateKey): Transaction =
    val txHash = transactionHash(dest, amount, nonce)
    val sig = sign(Hash.value(txHash), privateKey)
    Transaction(source, dest, amount, nonce, txHash, sig)
