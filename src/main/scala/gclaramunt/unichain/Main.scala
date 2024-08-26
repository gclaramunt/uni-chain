package gclaramunt.unichain

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = UnichainServerHttp4s.run[IO]
