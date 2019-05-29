package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt64, UInt8}
import scodec.bits.{BitVector, ByteVector}

import scala.annotation.tailrec

// TODO: Replace ByteVector with a type for keys
case class GolombFilter(
    key: ByteVector,
    m: UInt64,
    p: UInt8,
    encodedData: BitVector) {
  lazy val decodedHashes: Vector[UInt64] = GCS.golombDecodeSet(encodedData, p)

  lazy val n: Int = decodedHashes.length

  // TODO: Offer alternative that stops decoding when it finds out if data is there
  def matches(data: ByteVector): Boolean = {
    @tailrec
    def binarySearch(
        from: Int,
        to: Int,
        hash: UInt64,
        set: Vector[UInt64]): Boolean = {
      if (to < from) {
        false
      } else {
        val index = (to + from) / 2
        val otherHash = set(index)

        if (hash == otherHash) {
          true
        } else if (hash < otherHash) {
          binarySearch(from, index - 1, hash, set)
        } else {
          binarySearch(index + 1, to, hash, set)
        }
      }
    }

    val f = UInt64(n) * m
    val hash = GCS.hashToRange(data, f, key)

    binarySearch(from = 0, to = n - 1, hash, decodedHashes)
  }
}
