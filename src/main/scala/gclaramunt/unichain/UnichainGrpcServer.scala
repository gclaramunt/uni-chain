package gclaramunt.unichain

import cats.Applicative
import cats.effect.Resource
import helloword.HelloServiceGrpc.HelloService
import io.grpc.ServerServiceDefinition
import cats.effect.IO
import helloword.{HelloRequest, HelloResponse, HelloServiceFs2Grpc}
import org.checkerframework.checker.units.qual.A
import cats.syntax.all._

import scala.concurrent.Future
class Server {

  import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
  import fs2.grpc.syntax.all._


  val unichainService: Resource[IO, ServerServiceDefinition] =
    HelloServiceFs2Grpc.bindServiceResource[IO](new HelloServiceImpl())

  def run(service: ServerServiceDefinition) = NettyServerBuilder
    .forPort(9999)
    .addService(service)
    .resource[IO]
    .evalMap(server => IO(server.start()))
    .useForever

  unichainService.use(run)
}


class HelloServiceImpl[F[_]: Applicative, A] extends  HelloServiceFs2Grpc[F, A] {
  def sayHello(request: helloword.HelloRequest, ctx: A): F[HelloResponse]  = new HelloResponse("hello").pure
}