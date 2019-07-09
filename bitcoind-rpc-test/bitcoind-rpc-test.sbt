libraryDependencies ++= Deps.bitcoindRpcTest

name := "bitcoin-s-bitcoind-rpc-test"

lazy val downloadBinaries = taskKey[Unit]("Download bitcoind binaries")

downloadBinaries := {
  val logger = streams.value.log

  import java.nio.file._
  import scala.util.Properties

  def downloadForVersion(version: String) = {
    val binaryFolder = new File("bitcoind-binaries")
    val destination = binaryFolder / version
    val suffix =
      if (Properties.isWin) "win64.zip"
      else if (Properties.isMac) "osx64.tar.gz"
      else if (Properties.isLinux) "x86_64-linux-gnu.tar.gz"
      else sys.error(s"Unknown OS ${Properties.osName}")
    val fileName = s"bitcoin-$version-$suffix"
    val source =
      new URL(s"https://bitcoincore.org/bin/bitcoin-core-$version/$fileName")

    if (Files.notExists(destination.toPath)) {
      logger.info(s"$destination does not exist, creating")
      Files.createDirectories(destination.toPath)
    }

    val stream = source.openStream
    try {
      val destWithName = destination / fileName
      logger.info(s"Downloading $source to $destination")
      val res = IO.transfer(stream, destWithName)

      logger.info(s"Unzipping $destWithName")
      IO.unzip(destWithName, destination)

    } catch {
      case e: Throwable =>
        logger.error(s"Error when downloading $source: $e")
        throw e
    } finally {
      stream.close()
    }

    logger.info(s"Done")

  }

  downloadForVersion("0.17.0")
}
