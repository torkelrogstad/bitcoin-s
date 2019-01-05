package org.bitcoins.spvnode.messages.data

import org.bitcoins.core.util.BitcoinSLogger
import org.bitcoins.spvnode.gen.DataMessageGenerator
import org.scalacheck.{Prop, Properties}

/**
  * Created by chris on 6/29/16.
  */
class GetHeadersMessageSpec extends Properties("GetHeadersMessageSpec") with BitcoinSLogger {


  property("Serialization symmetry") =
    Prop.forAll(DataMessageGenerator.getHeaderMessages) { headerMsg =>
      GetHeadersMessage(headerMsg.hex) == headerMsg
    }
}
