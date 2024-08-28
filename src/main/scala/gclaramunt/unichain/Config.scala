package gclaramunt.unichain

import com.zaxxer.hikari.HikariConfig
import pureconfig.generic.derivation.default.*
import pureconfig.{ConfigReader, ConfigSource}

object Config:
  
  case class NodeConfig(db: DbConfig, crypto: CryptoConfig, transactionsPerBlock: Long) derives ConfigReader

  case class CryptoConfig(privateKey: String) derives ConfigReader

  case class DbConfig(driver: String, jdbcUrl: String, user: Option[String], password: Option[String], maxSessions: Int) derives ConfigReader

  lazy val nodeConfig: NodeConfig = ConfigSource
    .default
    .at("node")
    .loadOrThrow[NodeConfig]
  
  lazy val hikariConfig: HikariConfig = {
    val cfg = new HikariConfig()
    cfg.setDriverClassName(nodeConfig.db.driver)
    cfg.setJdbcUrl(nodeConfig.db.jdbcUrl)
    cfg.setUsername(nodeConfig.db.user.getOrElse(""))
    cfg.setPassword(nodeConfig.db.password.getOrElse(""))
    cfg
  }


  
