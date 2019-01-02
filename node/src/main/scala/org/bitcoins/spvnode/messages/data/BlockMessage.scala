package org.bitcoins.spvnode.messages.data

import org.bitcoins.core.protocol.blockchain.Block
import org.bitcoins.core.util.Factory
import org.bitcoins.spvnode.messages.BlockMessage
import org.bitcoins.spvnode.serializers.messages.data.RawBlockMessageSerializer
import scodec.bits.ByteVector

/**
  * Created by chris on 7/8/16.
  */
object BlockMessage extends Factory[BlockMessage] {

  private case class BlockMessageImpl(block: Block) extends BlockMessage

  def fromBytes(bytes: ByteVector): BlockMessage = RawBlockMessageSerializer.read(bytes)

  def apply(block: Block): BlockMessage = BlockMessageImpl(block)

}
