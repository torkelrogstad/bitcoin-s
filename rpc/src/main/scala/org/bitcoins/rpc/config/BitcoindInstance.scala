package org.bitcoins.rpc.config

import java.io.File
import java.net.URI

import org.bitcoins.core.config.NetworkParameters

import scala.sys.process._
import scala.util.Try

/**
  * Created by chris on 4/29/17.
  */
sealed trait BitcoindInstance {
  require(
    rpcUri.getPort == rpcPort,
    s"RpcUri and the rpcPort in authCredentials are different! RpcUri: $rpcUri authCredentials: $rpcPort")

  def binary: File
  require(binary.isFile && binary.canExecute,
          "bitcoind binary path must be a executable file")

  def network: NetworkParameters
  def uri: URI
  def rpcUri: URI
  def authCredentials: BitcoindAuthCredentials

  def rpcPort: Int = authCredentials.rpcPort
  def zmqPortOpt: Option[Int]
}

object BitcoindInstance {
  lazy val DEFAULT_BITCOIND_LOCATION: File = {
    val path = Try("which bitcoind".!!)
      .getOrElse(
        throw new RuntimeException("Couldn't locate bitcoind on the user path"))
    new File(path)
  }

  private case class BitcoindInstanceImpl(
      network: NetworkParameters,
      uri: URI,
      rpcUri: URI,
      authCredentials: BitcoindAuthCredentials,
      zmqPortOpt: Option[Int],
      binary: File = DEFAULT_BITCOIND_LOCATION)
      extends BitcoindInstance

  def apply(
      network: NetworkParameters,
      uri: URI,
      rpcUri: URI,
      authCredentials: BitcoindAuthCredentials,
      zmqPortOpt: Option[Int] = None,
      binary: File = DEFAULT_BITCOIND_LOCATION): BitcoindInstance = {
    BitcoindInstanceImpl(network,
                         uri,
                         rpcUri,
                         authCredentials,
                         zmqPortOpt,
                         binary)
  }
}
