package org.bitcoins.core.gcs

import org.bitcoins.core.number.{ UInt64, UInt8 }
import org.scalatest.FlatSpec

class GCSTest extends FlatSpec {

  behavior of "Golomb Coded Set"

  it must "encode and decode from a Golomb Coded Set" in {
    val p = UInt8.zero
    val delta = UInt64.one

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }
}
