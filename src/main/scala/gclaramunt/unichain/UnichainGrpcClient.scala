package gclaramunt.unichain

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.google.protobuf.ByteString
import gclaramunt.unichain.Config.nodeConfig
import gclaramunt.unichain.blockchain.BlockchainOps.buildTx
import gclaramunt.unichain.blockchain.CryptoOps.{decodePEMKeys, pubKeyToAddress}
import gclaramunt.unichain.blockchain.CryptoTypes.{Address, Hash, Sig}
import io.grpc.{ManagedChannel, Metadata}
import org.bouncycastle.util.encoders.Base64
import unichain.*

object UnichainGrpcClient extends IOApp  {

  def run(args: List[String]) =
    managedChannelResource.flatMap(UniChainServiceFs2Grpc.stubResource[IO]).use(svc => runProgram(svc, args))


  import fs2.grpc.syntax.all.*
  import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

  val managedChannelResource: Resource[IO, ManagedChannel] =
    NettyChannelBuilder
      .forAddress(nodeConfig.server.grpcServerAddress, nodeConfig.server.grpcPort)
      .usePlaintext()
      .resource[IO]

  def runProgram(stub: UniChainServiceFs2Grpc[IO,Metadata], args: List[String]): IO[ExitCode] =
    def sendLastBlockRequest(): IO[ExitCode] = stub.lastBlock(Empty(), new Metadata()).map(
      block =>
        println("Last emitted block:")
        println(s"id: ${block.id}, hash: ${Base64.toBase64String(block.hash.toByteArray)}, previous block hash: ${Base64.toBase64String(block.previousHash.toByteArray)}, signature: ${Base64.toBase64String(block.signature.toByteArray)} ")
        ExitCode.Success
    )

    def sendBalanceRequest(destinationAddress: String): IO[ExitCode] =
      stub.addressBalance(BalanceRequest(destinationAddress), new Metadata()).map(response =>
        val msg = response.balance.map(balance => s"Balance of address $destinationAddress = $balance").getOrElse(s"Balance for address $destinationAddress not found")
        println(msg)
        ExitCode.Success
      )

    def sendTxAddRequest(destinationAddress: String, amount: BigDecimal): IO[ExitCode] =
      for 
        txRequest <- IO.fromTry( 
          for 
            (walletPrvKey, walletPubKey) <- decodePEMKeys(System.getenv("WALLET_PRIVATE_KEY"))
            sourceAddress = pubKeyToAddress(walletPubKey)
            nonce = System.currentTimeMillis()
            tx <- buildTx(sourceAddress, Address(destinationAddress), amount, nonce, walletPrvKey)
          yield TxRequest(Address.value(tx.source), destinationAddress, amount.toString, nonce, ByteString.copyFrom(Hash.value(tx.hash)), ByteString.copyFrom(Sig.value(tx.signature)))
        )
        response <- stub.txAdd(txRequest, new Metadata())
      yield
            println(s"Response: ${response.message}")
            if response.success then
              println("Success")
              ExitCode.Success
            else
              println("Failure")
              ExitCode.Error
        

    args match {
      case Nil => sendLastBlockRequest()
      case List(addressStr) => sendBalanceRequest(addressStr)
      case List(addressStr, amountStr ) => sendTxAddRequest(addressStr, BigDecimal(amountStr))
      case _ => IO{
        println("Invalid argument")
        ExitCode.Error
      }
    }

}
