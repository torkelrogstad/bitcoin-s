package org.bitcoins.core.number

import org.bitcoins.core.protocol.NetworkElement
import org.bitcoins.core.util.{BitcoinSUtil, Factory, NumberUtil}
import scodec.bits.ByteVector

import scala.util.{Failure, Success, Try}

/**
  * Created by chris on 6/4/16.
  */
/**
  * This abstract class is meant to represent a signed and unsigned number in C
  * This is useful for dealing with codebases/protocols that rely on C's
  * unsigned integer types
  */
sealed abstract class Number[T <: Number[T]]
    extends NetworkElement
    with BasicArithmetic[T] {
  type A = BigInt

  /** The underlying scala number used to to hold the number */
  private[number] def underlying: A

  /** The amount of bytes needed to represent this number */
  private[number] def byteLength: Int

  /** The amounf of hex characters needed to repressent this number */
  private[number] def hexLength = byteLength * 2

  override final def hex: String = {
    val nonPadded = f"$underlying%x"
    val padding = List.fill(hexLength - nonPadded.length())('0')
    padding.mkString("") ++ nonPadded
  }

  def toInt: Int = toBigInt.bigInteger.intValueExact()
  def toLong: Long = toBigInt.bigInteger.longValueExact()
  def toBigInt: BigInt = underlying

  /**
    * This is used to determine the valid amount of bytes in a number
    * for instance a UInt8 has an andMask of 0xff
    * a UInt32 has an andMask of 0xffffffff
    */
  def andMask: BigInt

  /** Factory function to create the underlying T, for instance a UInt32 */
  // why??
  def apply: A => T

  override def +(num: T): T = apply(checkResult(underlying + num.underlying))
  override def -(num: T): T = apply(checkResult(underlying - num.underlying))
  override def *(factor: BigInt): T = apply(checkResult(underlying * factor))
  override def *(num: T): T = apply(checkResult(underlying * num.underlying))

  def >(num: T): Boolean = underlying > num.underlying
  def >=(num: T): Boolean = underlying >= num.underlying
  def <(num: T): Boolean = underlying < num.underlying
  def <=(num: T): Boolean = underlying <= num.underlying

  def <<(num: Int): T = this.<<(apply(num))
  def >>(num: Int): T = this.>>(apply(num))

  def <<(num: T): T = {
    checkIfInt(num).map { _ =>
      apply((underlying << num.toInt) & andMask)
    }.get
  }

  def >>(num: T): T = {
    //this check is for weird behavior with the jvm and shift rights
    //https://stackoverflow.com/questions/47519140/bitwise-shift-right-with-long-not-equaling-zero/47519728#47519728
    if (num.toLong > 63) apply(0)
    else {
      checkIfInt(num).map { _ =>
        apply(underlying >> num.toInt)
      }.get
    }
  }

  def |(num: T): T = apply(checkResult(underlying | num.underlying))
  def &(num: T): T = apply(checkResult(underlying & num.underlying))
  def unary_- : T = apply(-underlying)
  def unary_~ : T = apply(~underlying)

  /**
    * Checks if the given result is within the range
    * of this number type
    */
  private def checkResult(result: BigInt): A = {
    require((result & andMask) == result,
            "Result was out of bounds, got: " + result)
    result
  }

  /** Checks if the given nubmer is within range of a Int */
  private def checkIfInt(num: T): Try[Unit] = {
    if (num.toBigInt >= Int.MaxValue || num.toBigInt <= Int.MinValue) {
      Failure(
        new IllegalArgumentException(
          "Num was not in range of int, got: " + num))
    } else {
      Success(())
    }
  }

  override def bytes: ByteVector = BitcoinSUtil.decodeHex(hex)
}

/**
  * Represents a signed number in our number system
  * Instances of this are [[Int32]] or [[Int64]]
  */
sealed abstract class SignedNumber[T <: Number[T]] extends Number[T]

/**
  * Represents an unsigned number in our number system
  * Instances of this are [[UInt32]] or [[UInt64]]
  */
sealed abstract class UnsignedNumber[T <: Number[T]] extends Number[T] {
  // override def unary_~ : T = (~bytes)
}

/** This number type is useful for dealing with [[org.bitcoins.core.util.Bech32]]
  * related applications. The native encoding for Bech32 is a 5 bit number which
  * is what this abstraction is meant to  be used for
  */
sealed abstract class UInt5 extends UnsignedNumber[UInt5] {
  override private[number] val byteLength: Int = UInt5.byteLength

  override def apply: A => UInt5 = UInt5(_)

  override def andMask: BigInt = 0x1f

  def byte: Byte = toInt.toByte

  def toUInt8: UInt8 = UInt8(toInt)
}

sealed abstract class UInt8 extends UnsignedNumber[UInt8] {
  override private[number] val byteLength: Int = UInt8.byteLength

  override def apply: A => UInt8 = UInt8(_)

  override def andMask = 0xff

  def toUInt5: UInt5 = {
    //this will throw if not in range of a UInt5, come back and look later
    UInt5(toInt)
  }
}

/**
  * Represents a uint32_t in C
  */
sealed abstract class UInt32 extends UnsignedNumber[UInt32] {
  override private[number] val byteLength: Int = UInt32.byteLength

  override def apply: A => UInt32 = UInt32(_)

  override def andMask = 0xffffffffL

  override def unary_~ : UInt32 = UInt32(~bytes)
}

/**
  * Represents a uint64_t in C
  */
sealed abstract class UInt64 extends UnsignedNumber[UInt64] {
  override private[number] val byteLength: Int = UInt64.byteLength

  override def apply: A => UInt64 = UInt64(_)
  override def andMask = 0xffffffffffffffffL

  /**
    * Converts a [[BigInt]] to a 8 byte hex representation.
    * [[BigInt]] will only allocate 1 byte for numbers like 1 which require 1 byte, giving us the hex representation 01
    * this function pads the hex chars to be 0000000000000001
    * @param bigInt The number to encode
    * @return The hex encoded number
    */
  private def encodeHex(bigInt: BigInt): String = {
    val hex = BitcoinSUtil.encodeHex(bigInt)
    if (hex.length == 18) {
      //means that encodeHex(BigInt) padded an extra byte, giving us 9 bytes instead of 8
      hex.slice(2, hex.length)
    } else {
      val padding = for { _ <- 0 until 16 - hex.length } yield "0"
      padding.mkString ++ hex
    }
  }
}

/**
  * Represents a int32_t in C
  */
sealed abstract class Int32 extends SignedNumber[Int32] {
  override private[number] val byteLength: Int = Int32.byteLength
  override def apply: A => Int32 = Int32(_)
  override def andMask = 0xffffffff
}

/**
  * Represents a int64_t in C
  */
sealed abstract class Int64 extends SignedNumber[Int64] {
  override private[number] val byteLength: Int = Int64.byteLength
  override def apply: A => Int64 = Int64(_)
  override def andMask = 0xffffffffffffffffL
}

/**
  * Represents various numbers that should be implemented
  * inside of any companion object for a number
  */
trait BaseNumbers[T] {
  def zero: T
  def one: T
  def min: T
  def max: T
}

trait BaseNumbersWithImpl[T <: Number[T]] extends BaseNumbers[T] {
  self: NumberFactory[T] =>

  final lazy val zero: T = checkBounds(0)
  final lazy val one: T = checkBounds(1)
  final lazy val min: T = checkBounds(minUnderlying)
  final lazy val max: T = checkBounds(maxUnderlying)
}

trait NumberFactory[T <: Number[T]] extends Factory[T] {

  /** The amount of bytes needed to represent this number */
  private[number] def byteLength: Int

  protected def impl: T#A => T

  override def fromBytes(bytes: ByteVector): T = {
    require(bytes.length <= byteLength,
            s"Cannot construct a $name with ${bytes.length} bytes!")
    checkBounds(BigInt(bytes.toArray).toLong)
  }

  protected final def checkBounds(res: BigInt): T = {
    if (res > maxUnderlying || res < minUnderlying) {
      throw new IllegalArgumentException(
        s"Out of bounds for a $name, got: $res")
    } else impl(res)
  }

  def apply(long: Long): T = checkBounds(BigInt(long))
  def apply(bigInt: BigInt): T = checkBounds(bigInt)

  protected def minUnderlying: T#A
  protected def maxUnderlying: T#A

  protected def name: String = {
    val maybe = this.getClass().getSimpleName()
    // scalac appends $ to object names
    if (maybe.endsWith("$")) maybe.dropRight(1) else maybe
  }

  final def toBytes(nums: Seq[T]): ByteVector =
    nums.map(_.bytes).fold(ByteVector.empty)(_ ++ _)

}

trait UnsignedNumberFactory[T <: UnsignedNumber[T]] extends NumberFactory[T] {
  override def fromBytes(bytes: ByteVector): T = {
    require(bytes.size <= byteLength,
            s"Cannot construct a $name with ${bytes.size} bytes!")
    val res = NumberUtil.toUnsignedInt(bytes)
    checkBounds(res)
  }

  override protected final val minUnderlying: BigInt = 0
}

/** Reresents a number that fits in a byte */
trait ByteAble[T <: Number[T]] { self: NumberFactory[T] =>
  require(maxUnderlying <= (Byte.MaxValue + Math.abs(Byte.MinValue)),
          s"$name ($maxUnderlying) does not fit in a byte!")

  final def apply(byte: Byte): T = fromByte(byte)
  final def fromByte(byte: Byte): T = checkBounds(BigInt(byte))
  final def toByte(num: T): Byte = num.underlying.toByte
}

object UInt5
    extends UnsignedNumberFactory[UInt5]
    with BaseNumbersWithImpl[UInt5]
    with ByteAble[UInt5] {
  override private[number] val byteLength: Int = 1
  override protected def maxUnderlying: BigInt = 31

  private case class UInt5Impl(underlying: BigInt) extends UInt5
  override protected def impl: BigInt => UInt5 = UInt5Impl.apply _

  def toUInt5(b: Byte): UInt5 = {
    fromByte(b)
  }

  def toUInt5s(bytes: ByteVector): Vector[UInt5] = {
    bytes.toArray.map(toUInt5).toVector
  }
}

object UInt8
    extends UnsignedNumberFactory[UInt8]
    with BaseNumbersWithImpl[UInt8]
    with ByteAble[UInt8] {
  override private[number] val byteLength: Int = 1
  override protected def maxUnderlying: BigInt = 255

  private case class UInt8Impl(underlying: BigInt) extends UInt8
  override protected def impl: UInt8#A => UInt8 = UInt8Impl.apply _

  def toUInt8(byte: Byte): UInt8 = {
    fromBytes(ByteVector.fromByte(byte))
  }

  def toUInt8s(bytes: ByteVector): Vector[UInt8] = {
    bytes.toArray.map(toUInt8).toVector
  }

}

object UInt32
    extends UnsignedNumberFactory[UInt32]
    with BaseNumbersWithImpl[UInt32] {
  override private[number] val byteLength: Int = 4
  override protected def maxUnderlying: BigInt = 4294967295L

  private case class UInt32Impl(underlying: BigInt) extends UInt32
  override protected def impl: BigInt => UInt32 = UInt32Impl.apply _

}

object UInt64
    extends UnsignedNumberFactory[UInt64]
    with BaseNumbersWithImpl[UInt64] {
  override private[number] val byteLength: Int = 8
  override protected def maxUnderlying: BigInt = BigInt("18446744073709551615")

  private case class UInt64Impl(underlying: BigInt) extends UInt64
  override protected def impl: BigInt => UInt64 = UInt64Impl.apply _

}

object Int32 extends NumberFactory[Int32] with BaseNumbersWithImpl[Int32] {
  override private[number] val byteLength: Int = 4
  override protected val minUnderlying: BigInt = -2147483648
  override protected def maxUnderlying: BigInt = 2147483647

  private case class Int32Impl(underlying: BigInt) extends Int32
  override protected def impl: BigInt => Int32 = Int32Impl.apply _

}

object Int64 extends NumberFactory[Int64] with BaseNumbersWithImpl[Int64] {
  override private[number] val byteLength: Int = 8
  override protected val minUnderlying: BigInt = -9223372036854775808L
  override protected def maxUnderlying: BigInt = 9223372036854775807L

  private case class Int64Impl(underlying: BigInt) extends Int64
  override protected def impl: BigInt => Int64 = Int64Impl.apply _
}
