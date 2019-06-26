package org.bitcoins.core

import contextual._

package object protocol {
  implicit class Bech32AddressSyntax(private val sc: StringContext)
      extends AnyVal {
    def b32addr = Prefix(Bech32AddressInterpolator, sc)
  }
}
