package gclaramunt.unichain.store



import cats.effect.MonadCancelThrow
import doobie.*
import doobie.implicits.*
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.{Block, Transaction}

class LedgerDB[F[_]: MonadCancelThrow](xa: Transactor[F]):

  implicit val hashMeta: Meta[Hash] = Meta[Array[Byte]].imap(Hash.from)(Hash.value)
  implicit val sigMeta: Meta[Sig] = Meta[Array[Byte]].imap(Sig.apply)(Sig.value)
  implicit val addressMeta: Meta[Address] = Meta[String].imap(Address.apply)(Address.value)

  //ignore the sequence of tx 
  implicit val blockRead: Read[Block] = Read[(Long, Hash, Sig)].map:
    case (id, previousHash, signature) => Block(id, Seq.empty[Transaction], previousHash, signature)

  def getLastBlock: F[Block] = 
    sql"SELECT id, previous, current FROM blocks order by id desc limit 1"
      .query[Block].unique
      .transact(xa)

  def getTransactions: fs2.Stream[F, Transaction] = 
    sql"SELECT source, destination, amount, signature, hash, nonce FROM transactions"
      .query[Transaction]
      .stream
      .transact(xa)

  def addBlock(b: Block): F[Int] = 
    sql"insert into blocks (id, previous_hash, signature ) values (${b.id}, ${Hash.value(b.previousHash)}, ${Sig.value(b.signature)})".update.run
      .transact(xa)

  def addTransaction(blockId: Long, tx: Transaction): F[Int] =
    sql"insert into transactions (source, destination, amount, signature, hash, nonce ) values (${tx.source}, ${tx.destination}, ${tx.amount}, ${tx.signature}, ${tx.hash}, ${tx.nonce})"
        .update.run.transact(xa)
