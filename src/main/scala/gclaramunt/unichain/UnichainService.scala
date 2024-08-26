package gclaramunt.unichain

import cats.effect.Ref
import gclaramunt.unichain.blockchain.CryptoTypes.Address
import gclaramunt.unichain.blockchain.{Block, Transaction}

class UnichainService[F[_]](lastBlock: Ref[F, Block], ledgerStatus: Ref[F,Map[Address, BigDecimal]]):
  
  def submitTx(tx: Transaction): F[String] = ???

  def addressBalance(address: Address): F[BigDecimal] = ???
  
  def lastValidBlock(): F[Block] = ???
  
    
  //val ledgerStatus: Map[Address, BigDecimal]




