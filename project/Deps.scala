import sbt._

// ScalaJS triple-percentage
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Deps {

  object V {
    val bouncyCastle = "1.55"
    val logback = "1.0.13"
    val scalacheck = "1.14.0"
    val scalaTest = "3.0.5"
    val spray = "1.3.2"
    val zeromq = "0.4.3"
    val akkav = "10.1.7"
    val akkaStreamv = "2.5.21"
    val playv = "2.7.0"
    val scodecV = "1.1.6"
    val junitV = "0.11"
    val nativeLoaderV = "2.3.2"
    val typesafeConfigV = "1.3.3"
    val ammoniteV = "1.6.2"
    val asyncV = "0.9.7"
  }

  object Compile {

    val zeromq = Def.setting("org.zeromq" % "jeromq" % V.zeromq)
    val akkaHttp = Def.setting("com.typesafe.akka" %% "akka-http" % V.akkav)

    val akkaStream =
      Def.setting("com.typesafe.akka" %% "akka-stream" % V.akkaStreamv)
    val playJson = Def.setting("com.typesafe.play" %% "play-json" % V.playv)

    val typesafeConfig =
      Def.setting("com.typesafe" % "config" % V.typesafeConfigV)

    val logback = Def.setting("ch.qos.logback" % "logback-classic" % V.logback)

    val ammonite =
      "com.lihaoyi" %% "ammonite" % V.ammoniteV cross CrossVersion.full
  }

  object Test {

    val async =
      Def.setting("org.scala-lang.modules" %% "scala-async" % V.asyncV % "test")

    val bitcoinj = Def.setting(
      "org.bitcoinj" % "bitcoinj-core" % "0.14.4" % "test"
    )

    val logback =
      Def.setting("ch.qos.logback" % "logback-classic" % V.logback % "test")

    val scalacheck =
      Def.setting("org.scalacheck" %% "scalacheck" % V.scalacheck % "test")

    val scalaTest =
      Def.setting("org.scalatest" %% "scalatest" % V.scalaTest % "test")
    val spray = Def.setting("io.spray" %% "spray-json" % V.spray % "test")

    val akkaHttp =
      Def.setting("com.typesafe.akka" %% "akka-http-testkit" % V.akkav % "test")

    val akkaStream = Def.setting(
      "com.typesafe.akka" %% "akka-stream-testkit" % V.akkaStreamv % "test")
    val ammonite = Compile.ammonite % "test"
    val playJson = Def.setting(Compile.playJson.value % "test")
  }

  val root = List(
    Test.ammonite
  )

  lazy val coreCross = Def.setting(
    Seq(
      "org.scodec" %%% "scodec-bits" % V.scodecV,
      "com.outr" %%% "scribe" % "2.7.3"
    ))

  lazy val coreJVM = Def.setting(
    coreCross.value ++
      Seq(
        "org.bouncycastle" % "bcprov-jdk15on" % V.bouncyCastle
      ))

  lazy val coreJS = Def.setting(coreCross.value)

  val secp256k1jni = List(
    //for loading secp256k1 natively
    "org.scijava" % "native-lib-loader" % V.nativeLoaderV,
    "com.novocode" % "junit-interface" % V.junitV % "test"
  )

  val coreTest = List(
    Test.bitcoinj,
    "com.novocode" % "junit-interface" % V.junitV % "test",
    Test.logback,
    Test.scalaTest,
    Test.spray,
    Test.ammonite,
    Test.playJson
  )

  val bitcoindZmq = List(
    Compile.zeromq,
    Test.logback,
    Test.scalacheck,
    Test.scalaTest,
    Test.ammonite
  )

  val bitcoindRpc = List(
    Compile.akkaHttp,
    Compile.akkaStream,
    Compile.playJson,
    Compile.typesafeConfig,
    Test.ammonite
  )

  val bitcoindRpcTest = List(
    Test.akkaHttp,
    Test.akkaStream,
    Test.logback,
    Test.scalaTest,
    Test.scalacheck,
    Test.async,
    Test.ammonite
  )

  val bench = List(
    Compile.logback,
    Test.ammonite
  )

  val eclairRpc = List(
    Compile.akkaHttp,
    Compile.akkaStream,
    Compile.playJson,
    Test.ammonite
  )

  val eclairRpcTest = List(
    Test.akkaHttp,
    Test.akkaStream,
    Test.logback,
    Test.scalaTest,
    Test.scalacheck,
    Test.ammonite
  )

  val testkit = List(
    "org.scalacheck" %% "scalacheck" % V.scalacheck withSources () withJavadoc (),
    "org.scalatest" %% "scalatest" % V.scalaTest withSources () withJavadoc (),
    Test.ammonite
  )

  val scripts = List(
    Compile.ammonite,
    Compile.logback,
    Test.scalaTest,
    Test.logback
  )
}
