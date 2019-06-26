package org.bitcoins.core

import contextual._

package object script {
  implicit class ScriptStringContext(sc: StringContext) {
    val script = Prefix(ScriptInterpolator, sc)
  }
}
