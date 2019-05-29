package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt64, UInt8}
import org.bitcoins.core.util.NumberUtil
import org.bitcoins.testkit.util.BitcoinSUnitTest
import org.scalacheck.Gen
import scodec.bits.BitVector

class GCSTest extends BitcoinSUnitTest {
  behavior of "GCS"

  //https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#golomb-rice-coding
  it must "encode and decode Golomb Coded Set example 1" in {
    val p = UInt8(2)
    val original = UInt64.zero

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(false, false, false)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 2" in {
    val p = UInt8(2)
    val original = UInt64.one

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(false, false, true)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 3" in {
    val p = UInt8(2)
    val original = UInt64(2)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(false, true, false)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 4" in {
    val p = UInt8(2)
    val original = UInt64(3)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(false, true, true)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 5" in {
    val p = UInt8(2)
    val original = UInt64(4)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, false, false, false)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 6" in {
    val p = UInt8(2)
    val original = UInt64(5)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, false, false, true)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 7" in {
    val p = UInt8(2)
    val original = UInt64(6)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, false, true, false)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 8" in {
    val p = UInt8(2)
    val original = UInt64(7)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, false, true, true)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 9" in {
    val p = UInt8(2)
    val original = UInt64(8)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, true, false, false, false)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode Golomb Coded set example 10" in {
    val p = UInt8(2)
    val original = UInt64(9)

    val encoding = GCS.golombEncode(item = original, p = p)

    assert(encoding == BitVector.bits(Vector(true, true, false, false, true)))

    val decode = GCS.golombDecode(codedItem = encoding, p = p)

    assert(decode == original)
  }

  it must "encode and decode a golomb set for an arbitrary item and p" in {

    /**
      * Bit parameter for GCS, cannot be more than 32 as we will
      * have a number too large for a uint64.
      * [[https://github.com/Roasbeef/btcutil/blob/b5d74480bb5b02a15a9266cbeae37ecf9dd6ffca/gcs/gcs.go#L67]]
      */
    def genP: Gen[UInt8] = {
      Gen.choose(0, 32).map(UInt8(_))
    }

    def delta: Gen[UInt64] = {
      //what is a reasonable delta? This is means the delta
      //can be 1 - 16384
      //if we do a full uint64 it takes forever to encode it
      Gen
        .choose(1, NumberUtil.pow2(14).toInt)
        .map(UInt64(_))
    }

    forAll(delta, genP) {
      case (item, p) =>
        val encoded = GCS.golombEncode(item = item, p = p)
        val decode = GCS.golombDecode(codedItem = encoded, p = p)

        decode == item
    }
  }

}
