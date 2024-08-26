package gclaramunt.unichain

import cats.effect.{IO, IOApp}

object Genesis extends IOApp.Simple:
  val intialTreasury = BigDecimal(0)
  val treasuryAddress = ""
  val run =
    //emit block with one tx with initial treasury
