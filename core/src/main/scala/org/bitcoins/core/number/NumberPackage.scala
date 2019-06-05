package org.bitcoins.core

/**
  * This package provides Scala implementations of number formats
  * found in C and used througout the Bitcoin Core codebase.
  * Many data structures used in the Bitcoin protocol use
  * these number formats extensively. They therefore aid
  * signifcantly in representing Bitcoin data structures.
  */
package object number {

  /** Provides natural language syntax for unsigned 5-bit numbers */
  implicit class UInt5Int(private val underlying: Int) extends AnyVal {
    def uint5: UInt5 = UInt5(underlying)
    def ui5: UInt5 = UInt5(underlying)
  }

  /** Provides natural language syntax for unsigned 8-bit numbers */
  implicit class UInt8Int(private val underlying: Int) extends AnyVal {
    def uint8: UInt8 = UInt8(underlying)
    def ui8: UInt8 = UInt8(underlying)
  }
}
