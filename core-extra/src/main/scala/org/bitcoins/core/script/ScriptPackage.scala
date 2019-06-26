package org.bitcoins.core

import contextual._

package object script {

  /**
    * Defines a `script` string prefix that allows you
    * to write raw Bitcoin Script, as well as interpolate
    * values allowed in scripts, such as other script
    * tokens, raw bytes or things that can be serialized
    * to raw bytes.
    */
  implicit class ScriptStringSyntax(private val sc: StringContext)
      extends AnyVal {
    def script = Prefix(ScriptInterpolator, sc)
  }
}
