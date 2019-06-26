package org.bitcoins.core.script

import contextual._

import org.bitcoins.core.script.constant.ScriptToken
import org.bitcoins.core.serializers.script.ScriptParser
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import org.bitcoins.core.script.stack.OP_DUP
import org.bitcoins.core.protocol.NetworkElement
import scodec.bits.ByteVector
import scala.annotation.implicitNotFound

object ScriptInterpolator extends Interpolator {

  sealed abstract class AnythingGoes extends Context
  case object AnythingGoes extends AnythingGoes

  type ContextType = AnythingGoes
  type Output = Seq[ScriptToken]
  type Input = String

  def contextualize(interpolation: StaticInterpolation): Seq[ContextType] = {
    val contexts =
      interpolation.parts.foldLeft((List.empty[ContextType])) {
        case (contexts, lit @ Literal(_, string)) =>
          val newState = parseLiteral(string) match {
            case Failure(exception) =>
              interpolation.abort(lit, 0, exception.getMessage())
            case Success(value) => value
          }
          contexts
        case (contexts, hole @ Hole(_, _)) =>
          val newstate =
            hole(AnythingGoes).getOrElse(interpolation.abort(hole, ???))
          newstate :: contexts
      }
    val lit @ Literal(_, scriptString) = interpolation.parts.head
    try {
      ScriptParser.fromString(scriptString)
    } catch {
      case e: Throwable =>
        interpolation.abort(lit, 5, e.getMessage())
    }
    contexts
  }

  private def parseLiteral(string: String): Try[List[ScriptToken]] =
    Try { ScriptParser.fromString(string) }

  def evaluate(interpolation: RuntimeInterpolation) = {
    val scriptString = interpolation.parts.foldLeft("") {
      case (accum, Literal(_, string))     => accum + string
      case (accum, Substitution(_, value)) => accum + value
    }
    ScriptParser.fromString(scriptString)
  }

  implicit def embedTokenSeqs[T <: ScriptToken] =
    ScriptInterpolator.embed[Seq[T]](
      Case(AnythingGoes, AnythingGoes)(_.mkString(" "))
    )

  implicit def embedSingleTokens[T <: ScriptToken] =
    ScriptInterpolator.embed[T](
      Case(AnythingGoes, AnythingGoes)(_.toString)
    )

  implicit def embedNetworkElement[T <: NetworkElement] =
    ScriptInterpolator.embed[T](Case(AnythingGoes, AnythingGoes)(_.hex))

  implicit def embedBytes = ScriptInterpolator.embed[ByteVector](
    Case(AnythingGoes, AnythingGoes)(_.toHex)
  )

}
