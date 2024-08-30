val MunitVersion = "1.0.0"
val LogbackVersion = "1.5.6"
val MunitCatsEffectVersion = "2.0.0"
val PureConfigVersion = "0.17.7"

val BouncyCastleVersion = "1.78.1"
val DoobieVersion = "1.0.0-RC4"
val H2Version = "2.2.224"

lazy val root = (project in file("."))
  .settings(
    organization := "gclaramunt",
    name := "unichain",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.5.0",
    libraryDependencies ++= Seq(

      "com.h2database" % "h2" % H2Version,
      "org.tpolecat" %% "doobie-h2" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion, // HikariCP transactor.

      "com.github.pureconfig" %% "pureconfig-core" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,


      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      "org.bouncycastle" % "bcprov-jdk18on" % BouncyCastleVersion,
      "org.bouncycastle" % "bcpkix-jdk18on" % BouncyCastleVersion,

      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,

      //"org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCatsEffectVersion % Test,

    ),
    scalacOptions ++= Seq("-new-syntax", "-rewrite"),
    testFrameworks += new TestFramework("munit.Framework"),
    assembly / assemblyMergeStrategy  := {
      case PathList("META-INF", "services", _* ) =>MergeStrategy.first
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  )

enablePlugins(Fs2Grpc)

