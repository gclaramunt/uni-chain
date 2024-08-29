package gclaramunt.unichain.store

import cats.effect.MonadCancelThrow
import doobie.*
import doobie.implicits.*
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.{Block, Transaction}

trait LedgerDB[F[_]: MonadCancelThrow]:
  def getLastBlock: F[Block]
  def getTransactions: fs2.Stream[F, Transaction]
  def addBlock(b: Block): F[Int]
  def addTransaction(blockId: Long, tx: Transaction): F[Int]

object LedgerDB:
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F])= new LedgerDBImpl[F](xa)
  
class LedgerDBImpl[F[_]: MonadCancelThrow](xa: Transactor[F]) extends LedgerDB[F]:

  implicit val hashMeta: Meta[Hash] = Meta[Array[Byte]].imap(Hash.from)(Hash.value)
  implicit val sigMeta: Meta[Sig] = Meta[Array[Byte]].imap(Sig.apply)(Sig.value)
  implicit val addressMeta: Meta[Address] = Meta[String].imap(Address.apply)(Address.value)

  def getLastBlock: F[Block] = 
    sql"SELECT id, hash, previous_hash, signature FROM blocks order by id desc limit 1"
      .query[Block].unique
      .transact(xa)

  def getTransactions: fs2.Stream[F, Transaction] = 
    sql"SELECT source, destination, amount, nonce, hash, signature FROM transactions"
      .query[Transaction]
      .stream
      .transact(xa)

  def addBlock(b: Block): F[Int] = 
    sql"insert into blocks (id, hash, previous_hash, signature ) values (${b.id}, ${Hash.value(b.hash)}, ${Hash.value(b.previousHash)}, ${Sig.value(b.signature)})".update.run
      .transact(xa)

  def addTransaction(blockId: Long, tx: Transaction): F[Int] =
    sql"insert into transactions (source, destination, amount, nonce, hash, signature, block_id ) values (${tx.source}, ${tx.destination}, ${tx.amount}, ${tx.nonce}, ${tx.hash}, ${tx.signature}, $blockId)"
        .update.run.transact(xa)
