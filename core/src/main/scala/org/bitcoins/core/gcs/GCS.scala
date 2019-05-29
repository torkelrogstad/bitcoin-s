package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt64, UInt8}
import org.bouncycastle.crypto.macs.SipHash
import org.bouncycastle.crypto.params.KeyParameter
import scodec.bits.{BitVector, ByteVector}

import scala.annotation.tailrec

// TODO: Replace ByteVector with a type for keys
object GCS {

  def buildGCS(
      data: Vector[ByteVector],
      key: ByteVector,
      p: UInt8,
      m: UInt64): BitVector = {
    val hashedValues = hashedSetConstruct(data, key, m)
    val sortedHashedValues = hashedValues.sortWith(_ < _)
    encodeSortedSet(sortedHashedValues, p)
  }

  def buildGolombFilter(
      data: Vector[ByteVector],
      key: ByteVector,
      p: UInt8,
      m: UInt64): GolombFilter = {
    val encodedData = buildGCS(data, key, p, m)

    GolombFilter(key, m, p, encodedData)
  }

  def buildBasicBlockFilter(
      data: Vector[ByteVector],
      key: ByteVector): GolombFilter = {
    buildGolombFilter(data, key, p = UInt8(19), m = UInt64(784931))
  }

  private def sipHash(item: ByteVector, key: ByteVector): UInt64 = {
    val sh = new SipHash(2, 4)

    val keyParam = new KeyParameter(key.toArray)

    sh.init(keyParam)

    sh.update(item.toArray, 0, item.length.toInt)

    val digest = new Array[Byte](8)

    sh.doFinal(digest, 0)

    UInt64.fromBytes(ByteVector(digest))
  }

  def hashToRange(item: ByteVector, f: UInt64, key: ByteVector): UInt64 = {
    val hash = sipHash(item, key)

    val bigInt = (hash.toBigInt * f.toBigInt) >> 64

    UInt64(bigInt)
  }

  private def hashedSetConstruct(
      rawItems: Vector[ByteVector],
      key: ByteVector,
      m: UInt64): Vector[UInt64] = {
    val n = rawItems.length
    val f = m * n

    val hashedItemsBuilder = Vector.newBuilder[UInt64]

    rawItems.foreach { item =>
      val setValue = hashToRange(item, f, key)
      hashedItemsBuilder.+=(setValue)
    }

    hashedItemsBuilder.result()
  }

  def toUnary(num: UInt64): BitVector = {
    if (num == UInt64.zero) {
      BitVector.bits(Vector(false))
    } else {
      val binUnary = (BigInt(1) << num.toInt) - 1
      val leftPadded = BitVector(binUnary.toByteArray)
      val noPadding = dropLeftPadding(leftPadded)

      noPadding.:+(false)
    }
  }

  def golombEncode(item: UInt64, p: UInt8): BitVector = {
    val q = item >> p.toInt

    val prefix = toUnary(q)

    val pBits = item.bytes.toBitVector.takeRight(p.toInt)

    prefix ++ pBits
  }

  def golombDecode(codedItem: BitVector, p: UInt8): UInt64 = {
    @tailrec
    def split(vec: BitVector, accum: UInt64): (UInt64, BitVector) = {
      if (vec.head) {
        split(vec.tail, accum + UInt64.one)
      } else {
        (accum, vec.tail)
      }
    }

    val (q, pBits) = split(codedItem, UInt64.zero)

    val sizeWithPadding = (8 - (p.toInt % 8)) + p.toInt

    val pBitsAsBytes = {
      val withoutRightPaddingOrData = pBits.take(p.toInt)
      val withLeftPadding = withoutRightPaddingOrData.padLeft(sizeWithPadding)
      withLeftPadding.toByteVector
    }

    (q << p.toInt) + UInt64.fromBytes(pBitsAsBytes)
  }

  @tailrec
  private def dropLeftPadding(padded: BitVector): BitVector = {
    if (padded.isEmpty || padded.head) {
      padded
    } else {
      dropLeftPadding(padded.tail)
    }
  }

  private def golombDecodeItemFromSet(
      encodedData: BitVector,
      p: UInt8): (UInt64, BitVector) = {
    val head = golombDecode(encodedData, p)

    val prefixSize = (head >> p.toInt).toInt + 1

    (head, encodedData.drop(prefixSize + p.toInt))
  }

  def golombDecodeSet(encodedData: BitVector, p: UInt8): Vector[UInt64] = {
    @tailrec
    def loop(
        encoded: BitVector,
        decoded: Vector[UInt64],
        lastHash: UInt64 = UInt64.zero): Vector[UInt64] = {
      if (encoded.isEmpty) {
        decoded
      } else {
        val (delta, encodedLeft) = golombDecodeItemFromSet(encoded, p)
        val hash = lastHash + delta

        loop(encodedLeft, decoded.:+(hash), hash)
      }
    }

    loop(encodedData, Vector.empty)
  }

  def encodeSortedSet(hashes: Vector[UInt64], p: UInt8): BitVector = {
    val (golombStream, _) = hashes.foldLeft((BitVector.empty, UInt64.zero)) {
      case ((accum, lastHash), hash) =>
        val delta = hash - lastHash
        val encoded = golombEncode(delta, p)
        // TODO: is this order right? I think my encoding and decoding are backwards
        (accum ++ encoded, hash)
    }

    golombStream
  }
}
