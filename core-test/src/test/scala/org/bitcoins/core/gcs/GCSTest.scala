package org.bitcoins.core.gcs

import org.bitcoins.core.number.{ UInt64, UInt8 }
import org.scalatest.FlatSpec
import scodec.bits.BitVector

class GCSTest extends FlatSpec {

  behavior of "Golomb Coded Set"

  //https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#golomb-rice-coding
  it must "encode and decode Golomb Coded Set example 1" in {
    val p = UInt8(2)
    val delta = UInt64.zero

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(false, false, false)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 2" in {
    val p = UInt8(2)
    val delta = UInt64.one

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(false, false, true)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 3" in {
    val p = UInt8(2)
    val delta = UInt64(2)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(false, true, false)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 4" in {
    val p = UInt8(2)
    val delta = UInt64(3)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(false, true, true)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 5" in {
    val p = UInt8(2)
    val delta = UInt64(4)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, false, false, false)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 6" in {
    val p = UInt8(2)
    val delta = UInt64(5)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, false, false, true)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 7" in {
    val p = UInt8(2)
    val delta = UInt64(6)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, false, true, false)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 8" in {
    val p = UInt8(2)
    val delta = UInt64(7)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, false, true, true)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 9" in {
    val p = UInt8(2)
    val delta = UInt64(8)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, true, false, false, false)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }

  it must "encode and decode Golomb Coded set example 10" in {
    val p = UInt8(2)
    val delta = UInt64(9)

    val encoding = GCS.encode(
      delta = delta,
      p = p)

    assert(encoding == BitVector.bits(Vector(true, true, false, false, true)))

    val decode = GCS.decode(
      stream = encoding,
      p = p)

    assert(decode == delta)
  }
}
