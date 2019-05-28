package org.bitcoins.core

import org.bouncycastle.math.ec

package object crypto {

  abstract class ECPoint(
      curve: ec.ECCurve,
      x: ec.ECFieldElement,
      y: ec.ECFieldElement)
      extends ec.ECPoint(curve, x, y)
}
