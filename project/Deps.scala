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

    val akkaHttp = "com.typesafe.akka" %% "akka-http" % V.akkav

    val akkaStream = "com.typesafe.akka" %% "akka-stream" % V.akkaStreamv
    val playJson = "com.typesafe.play" %% "play-json" % V.playv

    val typesafeConfig = "com.typesafe" % "config" % V.typesafeConfigV

    val logback = "ch.qos.logback" % "logback-classic" % V.logback

    val ammonite =
      "com.lihaoyi" %% "ammonite" % V.ammoniteV cross CrossVersion.full
  }

  object Test {

    val async = "org.scala-lang.modules" %% "scala-async" % V.asyncV % "test"

    val bitcoinj = "org.bitcoinj" % "bitcoinj-core" % "0.14.4" % "test"

    val logback = "ch.qos.logback" % "logback-classic" % V.logback % "test"

    val spray = "io.spray" %% "spray-json" % V.spray % "test"

    val akkaHttp = "com.typesafe.akka" %% "akka-http-testkit" % V.akkav % "test"

    val akkaStream = "com.typesafe.akka" %% "akka-stream-testkit" % V.akkaStreamv % "test"
    val ammonite = Compile.ammonite % "test"
    val playJson = Compile.playJson % "test"
  }

  val root = List(
    Test.ammonite
  )

  lazy val coreCross = Def.setting(
    Seq(
      "org.scodec" %%% "scodec-bits" % V.scodecV,
      "com.outr" %%% "scribe" % "2.7.3",
      Test.ammonite,
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
    "com.novocode" % "junit-interface" % V.junitV % "test",
    Test.ammonite
  )

  val coreTest = List(
    Test.bitcoinj,
    "com.novocode" % "junit-interface" % V.junitV % "test",
    Test.logback,
    "org.scalatest" %% "scalatest" % V.scalaTest % "test",
    Test.spray,
    Test.ammonite,
    Test.playJson
  )

  val zmq =
    List(
      "org.zeromq" % "jeromq" % V.zeromq,
      "org.scalacheck" %% "scalacheck" % V.scalacheck % "test",
      "org.scalatest" %% "scalatest" % V.scalaTest % "test",
      Test.ammonite
    )

  val bitcoindRpc: List[librarymanagement.ModuleID] = List(
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
    "org.scalatest" %% "scalatest" % V.scalaTest % "test",
    "org.scalacheck" %% "scalacheck" % V.scalacheck % "test",
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
    "org.scalatest" %% "scalatest" % V.scalaTest % "test",
    "org.scalacheck" %% "scalacheck" % V.scalacheck % "test",
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
    "org.scalatest" %% "scalatest" % V.scalaTest % "test",
    Test.logback
  )
}
