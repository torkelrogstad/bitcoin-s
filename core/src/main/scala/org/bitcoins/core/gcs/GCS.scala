package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt64, UInt8}
import org.bouncycastle.crypto.macs.SipHash
import org.bouncycastle.crypto.params.KeyParameter
import scodec.bits.{BitVector, ByteVector}

import scala.annotation.tailrec

// TODO: Replace ByteVector with a type for keys
/**
  * Defines all functionality dealing with Golomb-Coded Sets as specified in
  * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#GolombCoded_Sets]]
  */
object GCS {

  /**
    * Given parameters and data, golomb-encodes the data as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#set-construction]]
    * @param data
    * @param key
    * @param p
    * @param m
    * @return
    */
  def buildGCS(
      data: Vector[ByteVector],
      key: ByteVector,
      p: UInt8,
      m: UInt64): BitVector = {
    val hashedValues = hashedSetConstruct(data, key, m)
    val sortedHashedValues = hashedValues.sortWith(_ < _)
    encodeSortedSet(sortedHashedValues, p)
  }

  /**
    * Given parameters and data, constructs a GolombFilter for that data
    * @param data
    * @param key
    * @param p
    * @param m
    * @return
    */
  def buildGolombFilter(
      data: Vector[ByteVector],
      key: ByteVector,
      p: UInt8,
      m: UInt64): GolombFilter = {
    val encodedData = buildGCS(data, key, p, m)

    GolombFilter(key, m, p, encodedData)
  }

  /**
    * Given data, constructs a GolombFilter for that data using parameters specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#block-filters]]
    * @param data
    * @param key
    * @return
    */
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

  /**
    * Hashes the item to the range [0, f) as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#hashing-data-objects]]
    * @param item
    * @param f
    * @param key
    * @return
    */
  def hashToRange(item: ByteVector, f: UInt64, key: ByteVector): UInt64 = {
    val hash = sipHash(item, key)

    val bigInt = (hash.toBigInt * f.toBigInt) >> 64

    UInt64(bigInt)
  }

  /**
    * Hashes the items of a set of items as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#hashing-data-objects]]
    * @param rawItems
    * @param key
    * @param m
    * @return
    */
  def hashedSetConstruct(
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

  /**
    * Converts num to unary (6 = 1111110) as required by
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#golomb-rice-coding]]
    * @param num
    * @return
    */
  def toUnary(num: UInt64): BitVector = {
    if (num == UInt64.zero) {
      BitVector.bits(Vector(false))
    } else {
      /*
       * We use the fact that 2^n - 1 = 111...1 (in binary) where there are n 1 digits
       */
      val binUnary = (BigInt(1) << num.toInt) - 1
      val leftPadded = BitVector(binUnary.toByteArray)
      val noPadding = dropLeftPadding(leftPadded)

      noPadding.:+(false)
    }
  }

  /**
    * Encodes a hash into a unary prefix and binary suffix as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#golomb-rice-coding]]
    * @param item
    * @param p
    * @return
    */
  def golombEncode(item: UInt64, p: UInt8): BitVector = {
    val q = item >> p.toInt

    val prefix = toUnary(q)

    val pBits = item.bytes.toBitVector.takeRight(p.toInt)

    prefix ++ pBits
  }

  /**
    * Decodes an item off of the front of a BitVector by reversing [[GCS.golombEncode]] as in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#golomb-rice-coding]]
    * @param codedItem
    * @param p
    * @return
    */
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

  /**
    * Decodes all hashes from golomb-encoded data, reversing [[GCS.encodeSortedSet]] as in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#set-queryingdecompression]]
    * @param encodedData
    * @param p
    * @return
    */
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

  /**
    * Given a set of ascending hashes, golomb-encodes them as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#set-construction]]
    * @param hashes
    * @param p
    * @return
    */
  def encodeSortedSet(hashes: Vector[UInt64], p: UInt8): BitVector = {
    val (golombStream, _) = hashes.foldLeft((BitVector.empty, UInt64.zero)) {
      case ((accum, lastHash), hash) =>
        val delta = hash - lastHash
        val encoded = golombEncode(delta, p)
        (accum ++ encoded, hash)
    }

    golombStream
  }
}
