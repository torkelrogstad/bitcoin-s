package org.bitcoins.spvnode.messages.control

import org.bitcoins.core.protocol.CompactSizeUInt
import org.bitcoins.core.util.Factory
import org.bitcoins.spvnode.messages.FilterAddMessage
import org.bitcoins.spvnode.serializers.messages.control.RawFilterAddMessageSerializer
import scodec.bits.ByteVector

/**
  * Created by chris on 8/26/16.
  * Factory object for a [[FilterAddMessage]]
  * [[https://bitcoin.org/en/developer-reference#filteradd]]
  */
object FilterAddMessage extends Factory[FilterAddMessage] {

  private case class FilterAddMessageImpl(elementSize: CompactSizeUInt, element: ByteVector) extends FilterAddMessage
  override def fromBytes(bytes: ByteVector): FilterAddMessage = RawFilterAddMessageSerializer.read(bytes)

  def apply(elementSize: CompactSizeUInt, element: ByteVector): FilterAddMessage = {
    FilterAddMessageImpl(elementSize,element)
  }
}
