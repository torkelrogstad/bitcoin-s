package org.bitcoins.node.networking.peer

import akka.actor.ActorRef
import akka.io.Tcp
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.crypto.DoubleSha256Digest
import org.bitcoins.core.util.BitcoinSLogger
import org.bitcoins.node.NetworkMessage
import org.bitcoins.node.messages._
import org.bitcoins.node.messages.control.{PongMessage, VersionMessage}
import org.bitcoins.node.messages.data.GetHeadersMessage
import org.bitcoins.node.models.Peer
import org.bitcoins.node.networking.Client

/**
  * Created by chris on 6/7/16.
  * This actor is the middle man between our [[Client]] and higher level actors such as
  * [[org.bitcoins.node.networking.BlockActor]]. When it receives a message, it tells [[Client]] to create connection to a peer,
  * then it exchanges [[VersionMessage]], [[VerAckMessage]] and [[org.bitcoins.node.messages.PingMessage]]/[[PongMessage]] message
  * with our peer on the network. When the Client finally responds to the [[NetworkMessage]] we originally
  * sent it sends that [[NetworkMessage]] back to the actor that requested it.
  */
class PeerMessageSender(client: Client)(implicit np: NetworkParameters)
    extends BitcoinSLogger {

  /** Initiates a connection with the given [[Peer]] */
  def connect(): Unit = {
    val socket = client.peer.socket
    (client.actor ! Tcp.Connect(socket))
  }

  def disconnect(): Unit = {
    (client.actor ! Tcp.Close)
  }

  /** Sends a [[org.bitcoins.node.messages.VersionMessage VersionMessage]] to our peer */
  def sendVersionMessage(): Unit = {
    val versionMsg = VersionMessage(client.peer.socket, np)
    sendMsg(versionMsg)
  }

  def sendVerackMessage(): Unit = {
    val verackMsg = VerAckMessage
    sendMsg(verackMsg)
  }

  def sendGetHeadersMessage(lastHash: DoubleSha256Digest): Unit = {
    val headersMsg = GetHeadersMessage(lastHash)
    sendMsg(headersMsg)
  }

  private def sendMsg(msg: NetworkPayload): Unit = {
    logger.debug(s"PeerMessageSender sending msg=${msg}")
    val newtworkMsg = NetworkMessage(np, msg)
    client.actor ! newtworkMsg
  }
}

object PeerMessageSender {

  private case class PeerMessageSenderImpl(client: Client)(
      implicit np: NetworkParameters)
      extends PeerMessageSender(client)(np)

  sealed abstract class PeerMessageHandlerMsg

  /**
    * For when we are done with exchanging version and verack messages
    * This means we can send normal p2p messages now
    */
  case object HandshakeFinished extends PeerMessageHandlerMsg

  case class SendToPeer(msg: NetworkMessage) extends PeerMessageHandlerMsg

  /** Accumulators network messages while we are doing a handshake with our peer
    * and caches a peer handler actor so we can send a [[HandshakeFinished]]
    * message back to the actor when we are fully connected
    *
    * @param networkMsgs
    * @param peerHandler
    */
  case class MessageAccumulator(
      networkMsgs: Vector[(ActorRef, NetworkMessage)],
      peerHandler: ActorRef)

  def apply(client: Client, np: NetworkParameters): PeerMessageSender = {
    PeerMessageSenderImpl(client)(np)
  }
}
