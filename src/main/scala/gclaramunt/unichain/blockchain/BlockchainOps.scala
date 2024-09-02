package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config.CryptoConfig
import gclaramunt.unichain.UnichainError.fromTry
import gclaramunt.unichain.blockchain.BlockchainOps.buildBlock
import gclaramunt.unichain.blockchain.CryptoOps.{addressToPubKey, decodePEMKeys, hash, sign}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, longToBytes}
import gclaramunt.unichain.{Config, GenericUnichainError, UnichainError}

import java.security.PrivateKey
import scala.util.Try

class BlockchainOps(pemKey: String):
  
  val (privateKey, publicKey) = decodePEMKeys(pemKey).get
  
  def newBlock(currentBlock: Block, memPool: Seq[Transaction]): Either[UnichainError, Block] =
    val currentBlockHash = currentBlock.hash
    for
      currentBlockValid <- fromTry(CryptoOps.validate(Hash.value(currentBlockHash), currentBlock.signature, publicKey))
      _ <- if !currentBlockValid then Left(GenericUnichainError("Invalid current block")) else Right(())
      newBlock <- fromTry(buildBlock(currentBlock.id + 1,memPool, currentBlockHash, privateKey ))
    yield newBlock


    
object BlockchainOps:

  def apply(cfg: CryptoConfig) = new BlockchainOps(cfg.privateKey)

  def validate(tx: Transaction): Try[Boolean] =
    for 
      pk <- addressToPubKey(tx.source) 
      isValid <-CryptoOps.validate(Hash.value(tx.hash), tx.signature, pk)
    yield isValid


  def blockHash(id: Long, txs: Seq[Transaction]): Try[Hash] =
    hash(longToBytes(id) ++ txs.flatMap(t => Hash.value(t.hash)))

  def transactionHash(destination: Address, amount: BigDecimal, nonce: Long): Try[Hash] =
    hash(Address.value(destination).getBytes ++ amount.toString().getBytes ++  longToBytes(nonce))

  def buildTx(source: Address, dest: Address, amount: BigDecimal, nonce: Long, privateKey: PrivateKey): Try[Transaction] =
    for
      txHash <- transactionHash(dest, amount, nonce)
      sig <- sign(Hash.value(txHash), privateKey)
    yield Transaction(source, dest, amount, nonce, txHash, sig)

  def buildBlock(newId: Long, memPool: Seq[Transaction], currentBlockHash: Hash, privateKey: PrivateKey): Try[Block] =
    for 
      newBlockHash <- blockHash(newId, memPool)
      // hash and sign the previous and current block hashes to prevent tampering
      hashData <- hash(Hash.value(currentBlockHash) ++ Hash.value(newBlockHash))
      signature <- sign(Hash.value(hashData), privateKey)
    yield Block(newId, hashData, currentBlockHash, signature)
