package org.bitcoins.spvnode.messages.data

import org.bitcoins.spvnode.gen.DataMessageGenerator
import org.scalacheck.{Prop, Properties}

/**
  * Created by chris on 7/8/16.
  */
class GetDataMessageSpec extends Properties("GetDataMessageSpec") {

  property("Serialization symmetry") =
    Prop.forAll(DataMessageGenerator.getDataMessages) { dataMsg =>
      GetDataMessage(dataMsg.hex) == dataMsg

    }
}
