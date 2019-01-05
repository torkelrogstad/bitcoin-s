package org.bitcoins.spvnode.serializers.messages.data

import org.bitcoins.core.protocol.blockchain.Block
import org.bitcoins.core.serializers.RawBitcoinSerializer
import org.bitcoins.spvnode.messages.BlockMessage
import org.bitcoins.spvnode.messages.data.BlockMessage
import scodec.bits.ByteVector

/**
  * Created by chris on 7/8/16.
  */
trait RawBlockMessageSerializer extends RawBitcoinSerializer[BlockMessage] {

  def read(bytes: ByteVector): BlockMessage = {
    val block = Block.fromBytes(bytes)
    BlockMessage(block)
  }

  def write(blockMsg: BlockMessage): ByteVector = blockMsg.block.bytes
}

object RawBlockMessageSerializer extends RawBlockMessageSerializer
