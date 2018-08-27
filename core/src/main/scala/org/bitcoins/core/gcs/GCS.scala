package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt32, UInt64, UInt8}
import org.bitcoins.core.protocol.NetworkElement
import org.bitcoins.core.util.Factory
import org.bouncycastle.crypto.macs.SipHash
import org.bouncycastle.crypto.params.KeyParameter
import scodec.bits.{BitVector, ByteVector}

import scala.util.{Failure, Try}

/**
 * Represents a Golomb-Coded Sets which is used as the data structure for Light Client's in BIP158
 * The exact specification can be found here
 * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#GolombCoded_Sets]]
 */
sealed abstract class GCS extends NetworkElement {

  /** The number of items in the set */
  def n: UInt32

  /** The encoding is also parameterized by P, the bit length of the remainder code. */
  def p: UInt8

  def modulusP: UInt64

  def modulusNP: UInt64

  def filterData: ByteVector

  override def bytes: ByteVector = ???

}

object GCS extends Factory[GCS] {
  private case class GCSImpl(
    n: UInt32,
    p: UInt8,
    modulusP: UInt64,
    modulusNP: UInt64,
    filterData: ByteVector) extends GCS

  override def fromBytes(bytes: ByteVector): GCS = {
    ???
  }

  def apply(
    n: UInt32,
    p: UInt8,
    modulusP: UInt64,
    modulusNP: UInt64,
    filterData: ByteVector): GCS = {
    GCSImpl(n, p, modulusP, modulusNP, filterData)
  }

  /**
   * Mimics this function for building a GCSFilter
   * [[https://github.com/Roasbeef/btcutil/blob/b5d74480bb5b02a15a9266cbeae37ecf9dd6ffca/gcs/gcs.go#L56]]
   */
  def build(p: UInt8, key: ByteVector, data: Vector[ByteVector]): Try[GCS] = {
    if (data.isEmpty) {
      Failure(new IllegalArgumentException(s"Cannot create a GCS filter with an empty data set"))
    } else if (data.length > UInt32.max.toInt) {
      Failure(new IllegalArgumentException(s"Cannot create a GCS filter because the filter is too big ${data.length}"))
    } else if (p > UInt8(32)) {
      Failure(new IllegalArgumentException(s"Cannot create a GCS filter because p was too large ${p}"))
    } else if (key.size != 16) {
      Failure(new IllegalArgumentException(s"Cannot create a GCS filter because the key was the wrong size, got ${key.size}"))
    } else {

      val modP = UInt64(1 << p.toInt)

      val modNP = UInt64(data.length) * modP

      val keyParam = new KeyParameter(key.toArray)

      val builder = Vector.newBuilder[UInt64]
      val hashedValuesBuilder = data.foldLeft(builder) {
        case (values, v) =>

          val sh = new SipHash()

          sh.init(keyParam)

          sh.update(v.toArray, 0, v.length.toInt)

          val digest = new Array[Byte](8)

          sh.doFinal(digest, 0)

          val u64 = UInt64.fromBytes(ByteVector(digest))

          //no modulo operator on UInt64 yet so have to use this hack
          val e = UInt64(u64.toBigInt % (modNP.toBigInt))


          values.+=(e)
      }

      val hashedValues: Vector[UInt64] = hashedValuesBuilder.result()

      val sorted = hashedValues.sortWith(_ <= _)

      val bitVector = buildBitVector(
        values = hashedValues,
        lastValue = UInt64.zero,
        remainder = UInt64.zero
      )

    }
  }

  private def buildBitVector(values: Vector[UInt64], lastValue: UInt64, remainder: UInt64): BitVector = {

  }

}
