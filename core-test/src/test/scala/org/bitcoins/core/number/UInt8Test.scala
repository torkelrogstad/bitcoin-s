package org.bitcoins.core.number

import org.scalatest.{FlatSpec, MustMatchers}
import scodec.bits.ByteVector
import org.bitcoins.testkit.core.gen.NumberGenerator
import org.bitcoins.testkit.util.BitcoinSUnitTest
import org.scalacheck.Gen
import org.scalacheck.Shrink
import scala.util.Try

class UInt8Test extends BitcoinSUnitTest {

  "UInt8" must "convert a byte to a UInt8 correctly" in {
    UInt8.toUInt8(0.toByte) must be(UInt8.zero)
    UInt8.toUInt8(1.toByte) must be(UInt8.one)
    UInt8.toUInt8(255.toByte) must be(UInt8(255.toShort))
  }

  it must "throw an exception if we try and create an UInt8 with more than 1 bytes" in {
    intercept[IllegalArgumentException] {
      UInt8(ByteVector(0.toByte, 0.toByte))
    }
  }

  it must "convert uint8 -> byte -> uint8" in {
    forAll(NumberGenerator.uInt8) {
      case u8: UInt8 =>
        assert(UInt8(UInt8.toByte(u8)) == u8)
    }
  }

  it must "have serialization symmetry" in {
    forAll(NumberGenerator.uInt8) { u8 =>
      assert(UInt8(u8.hex) == u8)
    }
  }
  it must "have the '<<' operator" in {
    implicit val noShrink: Shrink[Nothing] = Shrink.shrinkAny
    forAll(NumberGenerator.uInt8, Gen.choose(0, 8)) {
      case (u8: UInt8, shift: Int) =>
        val r = Try(u8 << shift)
        val expected = (u8.toLong << shift) & 0xffL
        if (expected <= UInt8.max.toLong) {
          assert(r.get == UInt8(expected.toShort))
        } else {
          assert(r.isFailure)
        }
    }
  }

  it must "have the '>>' operator " in {
    implicit val noShrink: Shrink[Nothing] = Shrink.shrinkAny
    forAll(NumberGenerator.uInt8, Gen.choose(0, 100)) {
      case (u8: UInt8, shift: Int) =>
        val r = (u8 >> shift)
        val expected =
          if (shift > 31) UInt8.zero else UInt8((u8.toLong >> shift).toShort)
        if (r != expected) {
          logger.warn("expected: " + expected)
          logger.warn("r: " + r)
        }
        assert(r == expected)
    }
  }

  it must "have Int syntax" in {
    forAll(NumberGenerator.uInt8) { u8 =>
      val int = u8.toInt
      assert(int.uint8 == u8)
      assert(int.ui8 == u8)
    }
  }
}
