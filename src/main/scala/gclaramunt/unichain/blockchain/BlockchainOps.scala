package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, privateKey, sign}

import java.security.{KeyFactory, PublicKey}
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import scala.util.Try


object BlockchainOps:

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
    // Create a specification for the key
    val keySpec = new X509EncodedKeySpec(keyBytes)
    // Get a KeyFactory for the EC (Elliptic Curve) algorithm
    val keyFactory = KeyFactory.getInstance("EC", "BC")
    // Generate the public key from the specification
    keyFactory.generatePublic(keySpec)