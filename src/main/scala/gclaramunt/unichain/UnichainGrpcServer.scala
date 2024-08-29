package gclaramunt.unichain

import cats.Applicative
import cats.effect.std.Dispatcher
import cats.effect.{Async, IO, IOApp, Resource}
import cats.syntax.all.*
import doobie.Transactor
import doobie.hikari.HikariTransactor.fromHikariConfig
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.Transaction
import gclaramunt.unichain.store.LedgerDB
import io.grpc.ServerServiceDefinition
import unichain.*

object UniChainServiceGrpcServer extends IOApp.Simple:

  import fs2.grpc.syntax.all.*
  import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder

  def unichainService[F[_]: Async](xa: Transactor[F], dispatcher: Dispatcher[F]): Resource[F,ServerServiceDefinition] =
    Resource.eval(UnichainService(LedgerDB(xa)).map( svc => UniChainServiceFs2Grpc.bindService(dispatcher, new UniChainServiceGrpcImpl(svc))))

  def runServer(service: ServerServiceDefinition): IO[Nothing] = NettyServerBuilder
    .forPort(9999)
    .addService(service)
    .resource[IO]
    .evalMap(server => IO(server.start()))
    .useForever

  val run: IO[Unit] = (for {
    xa <- fromHikariConfig[IO](Config.hikariConfig)
    dispatcher <- Dispatcher.parallel[IO]
    svc <-unichainService(xa, dispatcher)
  } yield svc).use(runServer)


class UniChainServiceGrpcImpl[F[_]: Applicative, A](svc: UnichainService[F]) extends  UniChainServiceFs2Grpc[F, A]:
  def txAdd(request: TxRequest, ctx: A): F[TxResponse] =
    val tx = Transaction(
      Address(request.source),
      Address(request.destination),
      BigDecimal(request.amount),
      request.nonce,
      Hash.from(request.hash.toByteArray),
      Sig(request.signature.toByteArray))
    svc.submitTx(tx).map { _ => TxResponse(true, "success") }

  def addressBalance(request: BalanceRequest, ctx: A): F[BalanceResponse] =
    svc.addressBalance(Address(request.address)).map(obd => BalanceResponse.of(obd.map(_.toString)))
