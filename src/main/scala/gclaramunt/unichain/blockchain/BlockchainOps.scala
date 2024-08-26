package gclaramunt.unichain.blockchain

import gclaramunt.unichain.Config
import gclaramunt.unichain.blockchain.CryptoTypes.{Hash, Sig}
import gclaramunt.unichain.blockchain.CryptoOps.{hash, sign}


object BlockchainOps {


  def blockHash(block: Block): Hash =
    hash(s"${block.id}${block.txs.map(_.hash)}")

  def newBlock(prevBlock: Block, memPool: Seq[Transaction]): Block =
    val prevBlockHash = blockHash(prevBlock)
    val newBlockHash = blockHash(prevBlock)
    val hash = prevBlockHash + newBlockHash
    val signature = Sig(sign(Hash.value(hash), Config.privateKey))
    Block(prevBlock.id+1, memPool, prevBlockHash, signature)



}
