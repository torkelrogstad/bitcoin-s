package org.bitcoins.core.protocol

import contextual._

object Bech32AddressInterpolator extends Interpolator {

  /** The different contexts we can be in */
  sealed abstract class AddressContext extends Context

  object AddressContext {
    case object Hrp extends AddressContext
    case object Data extends AddressContext
    case object Checksum extends AddressContext
  }

  type ContextType = AddressContext
  type Output = Bech32Address
  type Input = String

  // TODO: handle substitutions
  def contextualize(interpolation: StaticInterpolation): Seq[AddressContext] = {
    val lit @ Literal(_, str) = interpolation.parts.head
    try {
      Bech32Address.fromStringExn(str)
    } catch {
      case e: Throwable => interpolation.abort(lit, 0, e.getMessage())
    }
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation) = {
    val completeString = interpolation.parts.foldLeft("") {
      case (accum, Literal(_, lit))      => accum + lit
      case (accum, Substitution(_, sub)) => accum + sub
    }
    Bech32Address.fromStringExn(completeString)
  }

  // TODO: add typeclasses for allowed subs
  // HRP in first part
  // Seg[UInt5] for second and third
}
