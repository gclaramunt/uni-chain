package gclaramunt.unichain

import cats.Applicative
import cats.implicits.*
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.*

trait Transactions[F[_]]:
  def hello(n: Transactions.Name): F[Transactions.Greeting]

object Transactions:
  final case class Name(name: String) extends AnyVal
  /**
    * More generally you will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class Greeting(greeting: String) extends AnyVal
  object Greeting:
    given Encoder[Greeting] = new Encoder[Greeting]:
      final def apply(a: Greeting): Json = Json.obj(
        ("message", Json.fromString(a.greeting)),
      )

    given [F[_]]: EntityEncoder[F, Greeting] =
      jsonEncoderOf[F, Greeting]

  def impl[F[_]: Applicative]: Transactions[F] = new Transactions[F]:
    def hello(n: Transactions.Name): F[Transactions.Greeting] =
        Greeting("Hello, " + n.name).pure[F]
