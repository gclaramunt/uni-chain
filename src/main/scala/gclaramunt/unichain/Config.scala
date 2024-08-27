package gclaramunt.unichain

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

object Config:

  lazy val nodeConfig: NodeConfig = ConfigSource
    .default
    .at("node")
    .loadOrThrow[NodeConfig]
  
  case class NodeConfig(dbConfig: DbConfig, cryptoConfig: CryptoConfig, transactionsPerBlock: Long) derives ConfigReader

  case class CryptoConfig(privateKey: String) derives ConfigReader

  case class DbConfig(driver: String, jdbUrl: String, user: String, password: Option[String], maxSessions: Int) derives ConfigReader


  
