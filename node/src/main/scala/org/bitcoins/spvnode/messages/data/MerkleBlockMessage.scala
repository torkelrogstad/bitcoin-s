package org.bitcoins.spvnode.messages.data

import org.bitcoins.core.protocol.blockchain.MerkleBlock
import org.bitcoins.core.util.Factory
import org.bitcoins.spvnode.messages.MerkleBlockMessage
import org.bitcoins.spvnode.serializers.messages.data.RawMerkleBlockMessageSerializer
import scodec.bits.ByteVector

/**
  * Created by chris on 6/2/16.
  * https://bitcoin.org/en/developer-reference#merkleblock
  */
object MerkleBlockMessage extends Factory[MerkleBlockMessage] {

  private case class MerkleBlockMessageImpl(merkleBlock : MerkleBlock) extends MerkleBlockMessage

  def fromBytes(bytes : ByteVector) : MerkleBlockMessage = RawMerkleBlockMessageSerializer.read(bytes)

  def apply(merkleBlock: MerkleBlock) : MerkleBlockMessage = {
    MerkleBlockMessageImpl(merkleBlock)
  }
}
