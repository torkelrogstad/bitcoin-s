package org.bitcoins.core.protocol

import org.bitcoins.testkit.util.BitcoinSUnitTest

class Bech32AddressInterpolatorTest extends BitcoinSUnitTest {
  behavior of """the "b32addr" interpolator"""

  it must "work on a literal BTC mainnet address" in {
    val str = "bc1q70y3jgdfzhj2w32j2qqsa5auwlez4rq65y9rll"
    val addr = b32addr"bc1q70y3jgdfzhj2w32j2qqsa5auwlez4rq65y9rll"

    assert(Bech32Address.fromStringExn(str) == addr)
  }

  it must "work on a literal BTC testnet address" in {
    val str = "tb1qkunxewk8e86re59f0hhycpgr6xa4llq4h0exsk"
    val addr = b32addr"tb1qkunxewk8e86re59f0hhycpgr6xa4llq4h0exsk"
    assert(Bech32Address.fromStringExn(str) == addr)
  }

  it must "work on a literal BTC regtest address" in {
    val str = "bcrt1ql435qvam5vwjxvwymnd3wcla6yjmvvvwjpzeh3"
    val addr = b32addr"bcrt1ql435qvam5vwjxvwymnd3wcla6yjmvvvwjpzeh3"

    assert(Bech32Address.fromStringExn(str) == addr)
  }

  it must "work with HRP interpolations" in ???

  it must "fail with HRP interpolations in the wrong place" in ???

  it must "work with UInt5 interpolations" in ???

  it must "fail with UInt5 interpolations in the wrong place" in ???

  it must "fail on a bad string" in {
    assertDoesNotCompile("""
      b32addr"foobar"
      """)
  }

  it must "fail on a legacy address" in {
    assertDoesNotCompile("""
      b32addr"3QwQAXiCVKfwHzEfd2qn6Mtd6J8GTnQxfE"
      """)

  }
}
