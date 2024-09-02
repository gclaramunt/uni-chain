package gclaramunt.unichain

import cats.Applicative
import cats.effect.std.Dispatcher
import cats.effect.{Async, IO, IOApp, Resource}
import cats.syntax.all.*
import com.google.protobuf.ByteString
import doobie.Transactor
import doobie.hikari.HikariTransactor.fromHikariConfig
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import gclaramunt.unichain.blockchain.Transaction
import gclaramunt.unichain.store.LedgerDB
import io.grpc.{Server, ServerServiceDefinition}
import unichain.*

object UnichainServiceGrpcServer extends IOApp.Simple:

  import fs2.grpc.syntax.all.*
  import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder

  def unichainService[F[_]: Async](xa: Transactor[F], dispatcher: Dispatcher[F]): Resource[F,ServerServiceDefinition] =
    Resource.eval(UnichainService(LedgerDB(xa)).map( svc => UniChainServiceFs2Grpc.bindService(dispatcher, new UniChainServiceGrpcImpl(svc))))

  def serverResource(service: ServerServiceDefinition): Resource[IO,Server] = NettyServerBuilder
    .forPort(Config.nodeConfig.server.grpcPort)
    .addService(service)
    .resource[IO]
    .evalMap(server => IO {
      val s= server.start()
      println("Server started")
      s
    })

  val run: IO[Unit] = (for
    xa <- fromHikariConfig[IO](Config.hikariConfig)
    dispatcher <- Dispatcher.parallel[IO]
    svc <-unichainService(xa, dispatcher)
    server <- serverResource(svc)
  yield server).useForever.recover { e => println(e)}


class UniChainServiceGrpcImpl[F[_]: Applicative, A](svc: UnichainService[F]) extends  UniChainServiceFs2Grpc[F, A]:
  def txAdd(request: TxRequest, ctx: A): F[TxResponse] =
    val tx = Transaction(
      Address(request.source),
      Address(request.destination),
      BigDecimal(request.amount),
      request.nonce,
      Hash.from(request.hash.toByteArray),
      Sig(request.signature.toByteArray))
    svc.submitTx(tx).map: 
      _ => TxResponse(true, "success") 

  def addressBalance(request: BalanceRequest, ctx: A): F[BalanceResponse] =
    svc.addressBalance(Address(request.address)).map(obd => BalanceResponse.of(obd.map(_.toString)))

  def lastBlock(request: Empty, ctx: A): F[BlockResponse] =
    def hashToByteString(h:Hash) = ByteString.copyFrom(Hash.value(h))
    svc.lastValidBlock().map(block => BlockResponse(block.id,hashToByteString(block.hash), hashToByteString(block.previousHash), ByteString.copyFrom(Sig.value(block.signature))))


