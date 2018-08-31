package org.bitcoins.core.gcs

import org.bitcoins.core.gen.NumberGenerator
import org.scalacheck.{ Prop, Properties }

class GCSSpec extends Properties("GCSSpec") {

  property("GCS serialization symmetry encode/decode") = {
    Prop.forAll(NumberGenerator.uInt64s, NumberGenerator.uInt8) {
      case (delta, p) =>
        val encoded = GCS.encode(
          delta = delta,
          p = p)

        val decode = GCS.decode(
          stream = encoded,
          p = p)

        decode == delta
    }
  }
}
