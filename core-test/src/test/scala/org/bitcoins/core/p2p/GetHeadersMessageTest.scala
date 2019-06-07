package org.bitcoins.core.p2p

import org.bitcoins.testkit.core.gen.p2p.DataMessageGenerator
import org.bitcoins.testkit.util.BitcoinSUnitTest

class GetHeadersMessageTest extends BitcoinSUnitTest {

  it must "have serialization symmetry" in {
    forAll(DataMessageGenerator.getHeaderMessages) { headerMsg =>
      assert(GetHeadersMessage(headerMsg.hex) == headerMsg)
    }
  }

  it must "be constructable from just hashes" in {
    forAll(DataMessageGenerator.getHeaderDefaultProtocolMessage) { getHeader =>
      assert(
        GetHeadersMessage(getHeader.hashes, getHeader.hashStop) == getHeader)
    }
  }
}
