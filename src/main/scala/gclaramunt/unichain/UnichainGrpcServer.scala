package gclaramunt.unichain

import cats.Applicative
import cats.effect.Resource
import io.grpc.ServerServiceDefinition
import cats.effect.IO
import org.checkerframework.checker.units.qual.A
import cats.syntax.all.*
import unichain.{HelloRequest, HelloResponse, UniChainServiceFs2Grpc}

import scala.concurrent.Future
class Server {

  import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
  import fs2.grpc.syntax.all._


  val unichainService: Resource[IO, ServerServiceDefinition] =
    UniChainServiceFs2Grpc.bindServiceResource[IO](new HelloServiceImpl())

  def run(service: ServerServiceDefinition) = NettyServerBuilder
    .forPort(9999)
    .addService(service)
    .resource[IO]
    .evalMap(server => IO(server.start()))
    .useForever

  unichainService.use(run)
}


class HelloServiceImpl[F[_]: Applicative, A] extends  UniChainServiceFs2Grpc[F, A] {
  def sayHello(request: HelloRequest, ctx: A): F[HelloResponse]  = new HelloResponse("hello").pure
}