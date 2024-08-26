package gclaramunt.unichain

import cats.effect.Trace
import cats.effect.std.Console
import com.zaxxer.hikari.HikariConfig

package object store:

  // TODO read from config
  lazy val hikariConfig: HikariConfig = {
    val cfg = new HikariConfig()
    cfg.setDriverClassName("org.h2.Driver")
    cfg.setJdbcUrl("jdbc:h2:~/ledger")
    cfg.setUsername("")
    cfg.setPassword("")
    cfg
  }