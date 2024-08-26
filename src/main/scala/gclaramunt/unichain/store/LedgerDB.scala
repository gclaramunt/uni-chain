package gclaramunt.unichain.store



import cats.effect.MonadCancelThrow
import doobie.implicits.toSqlInterpolator
import gclaramunt.unichain.blockchain.{Block, Transaction}
import doobie.implicits.*
import doobie.*
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}

class LedgerDB[F[_]: MonadCancelThrow](xa: Transactor[F]):

//  case class Block(id: Long, txs: Seq[Transaction], previous: Hash, current: Sig)
// case class Transaction(source: Address, destination: Address, amount: BigDecimal, signature: Sig, hash: Hash, nonce: Long)

  //  CREATE TABLE IF NOT EXISTS block (
  //    id   BIGINT,
  //    previous_hash VARBINARY,
  //    signature VARBINARY
  //  );
  //  CREATE TABLE IF NOT EXISTS transaction (
  //    source      VARCHAR,
  //    destination VARCHAR,
  //    amount NUMERIC,
  //    signature VARBINARY,
  //    hash VARBINARY,
  //    nonce   BIGINT,
  //    block_id BIGINT
  //  );

  implicit val hashMeta: Meta[Hash] = Meta[Array[Byte]].imap(Hash.apply)(Hash.value)
  implicit val sigMeta: Meta[Sig] = Meta[Array[Byte]].imap(Sig.apply)(Sig.value)
  implicit val addressMeta: Meta[Address] = Meta[String].imap(Address.apply)(Address.value)

  //ignore the sequence of tx 
  implicit val blockRead: Read[Block] = Read[(Long, Hash, Sig)].map:
    case (id, previousHash, signature) => Block(id, Seq.empty[Transaction], previousHash, signature)

  def getLastBlock(): F[Option[Block]] = 
    sql"SELECT id, previous, current FROM blocks order by id desc limit 1"
      .query[Block]
      .option
      .transact(xa)

  def getTransactions(): fs2.Stream[F, Transaction] = 
    sql"SELECT source, destination, amount, signature, hash, nonce FROM transactions"
      .query[Transaction]
      .stream
      .transact(xa)

  def addBlock(b: Block): F[Int] = 
    sql"insert into block (id, previous_hash, signature ) values (${b.id}, ${Hash.value(b.previousHash)}, ${Sig.value(b.signature)})".update.run
      .transact(xa)

  def addTransaction(blockId: Long, txs: Seq[Transaction]): F[Int] = 
    val sql = "insert into transaction (source, destination, amount, signature, hash, nonce ) values (?,?,?,?, ?,?, ${blockId})"
    Update[Transaction](sql).updateMany(txs).transact(xa)
