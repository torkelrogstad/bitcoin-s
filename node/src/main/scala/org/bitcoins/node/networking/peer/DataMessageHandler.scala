package org.bitcoins.node.networking.peer

import org.bitcoins.chain.api.ChainApi
import org.bitcoins.chain.blockchain.ChainHandler
import org.bitcoins.chain.config.ChainAppConfig
import org.bitcoins.chain.models.BlockHeaderDAO
import org.bitcoins.core.util.{BitcoinSLogger, FutureUtil}
import org.bitcoins.node.messages.{
  DataPayload,
  HeadersMessage,
  InventoryMessage
}

import scala.concurrent.{ExecutionContext, Future}
import org.bitcoins.core.protocol.transaction.Transaction

import DataMessageHandler._
import org.bitcoins.node.messages.TypeIdentifier.MsgBlock
import org.bitcoins.node.messages.TypeIdentifier.MsgFilteredBlock
import org.bitcoins.node.messages.TypeIdentifier.MsgTx
import org.bitcoins.node.messages.MsgUnassigned
import org.bitcoins.node.messages.GetDataMessage
import org.bitcoins.node.messages.data.GetDataMessage
import org.bitcoins.node.messages.data.Inventory
import org.bitcoins.node.messages.TransactionMessage
import org.bitcoins.node.messages.MerkleBlockMessage

/** This actor is meant to handle a [[org.bitcoins.node.messages.DataPayload]]
  * that a peer to sent to us on the p2p network, for instance, if we a receive a
  * [[HeadersMessage]] we should store those headers in our database
  */
class DataMessageHandler(
    onTxReceived: OnTxReceived = noopTxReceived
)(implicit ec: ExecutionContext, appConfig: ChainAppConfig)
    extends BitcoinSLogger {

  private val blockHeaderDAO: BlockHeaderDAO = BlockHeaderDAO(appConfig)

  def handleDataPayload(
      payload: DataPayload,
      peerMsgSender: PeerMessageSender): Future[Unit] = {
    payload match {
      case headersMsg: HeadersMessage =>
        val headers = headersMsg.headers
        val chainApi: ChainApi =
          ChainHandler(blockHeaderDAO, chainConfig = appConfig)
        val chainApiF = chainApi.processHeaders(headers)

        chainApiF.map { _ =>
          val lastHash = headers.last.hash
          peerMsgSender.sendGetHeadersMessage(lastHash)
        }
      case txMsg: TransactionMessage =>
        onTxReceived(txMsg.transaction)
        FutureUtil.unit
      case _: MerkleBlockMessage =>
        // verify merkleblock
        ???
      case invMsg: InventoryMessage =>
        handleInventoryMsg(invMsg = invMsg, peerMsgSender = peerMsgSender)
    }
  }

  /**
    * We send `getdata` requests for all filtered blocks.
    * That means we request a `merkleblock` message, given we
    * have configured a bloom filter with this peer prior to this
    */
  private def handleInventoryMsg(
      invMsg: InventoryMessage,
      peerMsgSender: PeerMessageSender): Future[Unit] = {
    logger.info(
      s"Received inv message. Inventory count: ${invMsg.inventoryCount.toInt}")
    logger.info(
      s"First 5 inventories: ${invMsg.inventories.take(5).mkString(", ")}")

    val inventories: Seq[Inventory] = {
      val listOfOpts = for {
        inventory <- invMsg.inventories
      } yield {
        inventory.typeIdentifier match {
          case MsgBlock         => None
          case MsgFilteredBlock => Some(inventory)
          case MsgTx            => None
          case _: MsgUnassigned => None
        }
      }
      listOfOpts.flatten
    }

    val getdata = GetDataMessage(inventories)
    logger.info(s"Sending getdata message for ${inventories.mkString(", ")}")
    peerMsgSender.sendMsg(getdata)

    FutureUtil.unit

  }
}

object DataMessageHandler {

  /** Callback for handling a received transaction */
  type OnTxReceived = Transaction => Unit

  /** Does nothing with the received transaction */
  val noopTxReceived: OnTxReceived = _ => ()

}
