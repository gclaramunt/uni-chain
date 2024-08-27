package gclaramunt.unichain.blockchain

import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}

import java.nio.ByteBuffer
import scala.annotation.targetName



object CryptoTypes {

  opaque type Address = String
  object Address:
    def apply(s: String): Address = s
    def value(s: Address): String = s

  opaque type Hash = Array[Byte]
  object Hash:
    //def apply(s: String): Hash = s.getBytes
    def apply(s: Array[Byte]): Hash = s
    def value(s: Hash): Array[Byte] = s

  extension (h: Hash)
    def +(h2: Hash): Hash = h ++ h2

  opaque type Sig = Array[Byte]
  object Sig:
    def apply(s: Array[Byte]): Sig = s
    def value(s: Sig): Array[Byte] = s


  def longToBytes(x: Long): Array[Byte] = {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.putLong(x)
    buffer.array
  }
}

/*
Transaction:
   Source, destination addresses for moving funds
   Amount being transferred
   Signature from the source signing the hash of all data within the transaction corresponding to the correct source address private key.
   Hash of the transaction should be what is being signed by the signature.
   Nonce or ordinal associated with the number of sequential transactions used for the source address, monotonically increasing and used to prevent duplicate replay transactions.
 */
case class TransactionCore(source: Address, destination: Address, amount: BigDecimal, nonce: Long) 
case class Transaction(source: Address, destination: Address, amount: BigDecimal, nonce: Long, hash: Hash,  signature: Sig)
object Transaction {
  def apply(core: TransactionCore, hash: Hash, sig: Sig): Transaction = new Transaction(core.source, core.destination, core.amount, core.nonce, hash, sig) 
}
/*
Block:
   Collection of or sequence of transactions
   Monotonically increasing block sequence number
   Reference to prior block hash
   Signature attached corresponding to hash of the data of the block signed by the keypair of the current (single process) node.
   Invalid to contain multiple conflicting transactions
 */
case class Block (id: Long, txs: Seq[Transaction], previousHash: Hash, signature: Sig)

/*
Address:
  Corresponds to the data associated with a public key
  Should have some notion of an associated balance (which is updated by blocks)
 */
case class AddressAccount(address: Address, balance: BigDecimal)

/*
Balances / Ledger State
  Information about the total currency amount as of the latest block per account / address.
*/
case class LedgerState(accounts: Map[Address, AddressAccount])