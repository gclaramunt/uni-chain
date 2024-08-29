package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.Config.CryptoConfig
import gclaramunt.unichain.blockchain.BlockchainOps.buildBlock
import gclaramunt.unichain.blockchain.CryptoOps.{addressToPubKey, decodePEMKeys, hash, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, longToBytes}

import java.security.{PrivateKey, PublicKey}
import scala.util.Try

class BlockchainOps(pemKey: String):
  
  val (privateKey, publicKey) = decodePEMKeys(pemKey)
  
  def newBlock(currentBlock: Block, memPool: Seq[Transaction]): Try[Block] =
    val currentBlockHash = currentBlock.hash
    CryptoOps.validate(Hash.value(currentBlockHash), currentBlock.signature, publicKey).map:
      currentBlockValid =>
        if (currentBlockValid) then
          buildBlock(currentBlock.id + 1,memPool, currentBlockHash, privateKey )
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

  def buildBlock(newId: Long, memPool: Seq[Transaction], currentBlockHash: Hash, privateKey: PrivateKey): Block =
    val newBlockHash = blockHash(newId, memPool)
    // hash and sign the previous and current block hashes to prevent tampering
    val hashData = hash(Hash.value(currentBlockHash) ++ Hash.value(newBlockHash))
    val signature = sign(Hash.value(hashData), privateKey)
    Block(newId, newBlockHash, currentBlockHash, signature)
