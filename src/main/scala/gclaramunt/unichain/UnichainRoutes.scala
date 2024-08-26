package gclaramunt.unichain

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object UnichainRoutes:

  def helloWorldRoutes[F[_]: Sync](H: Transactions[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(Transactions.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
