package org.bitcoins.core.script

import org.bitcoins.testkit.util.BitcoinSUnitTest

import org.scalatest._
import org.bitcoins.core.script.stack.OP_DUP
import org.bitcoins.core.script.crypto.OP_CHECKMULTISIGVERIFY
import scodec.bits._
import org.bitcoins.core.crypto.ECPublicKey
import org.bitcoins.core.script.crypto.OP_HASH160
import org.bitcoins.core.script.control._
import org.bitcoins.core.script.constant.BytesToPushOntoStack
import org.bitcoins.core.script.constant.ScriptConstant
import org.bitcoins.core.script.bitwise.OP_EQUALVERIFY
import org.bitcoins.core.script.crypto.OP_CHECKSIG
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.testkit.core.gen.AddressGenerator
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.testkit.core.gen.TransactionGenerators

class ScriptInterpolatorTest extends BitcoinSUnitTest {
  behavior of "the \"script\" interpolator"

  it must "parse basic scripts" in {
    val script = script"OP_DUP OP_HASH160"

    assert(script == List(OP_DUP, OP_HASH160))
  }

  it must "allow scripts with embedded tokens" in {
    val single = script"OP_HASH160 ${OP_DUP} OP_IF"
    assert(
      single == Seq(
        OP_HASH160,
        OP_DUP,
        OP_IF
      ))

    val multi = script"OP_HASH160 ${Seq(OP_DUP, OP_CHECKMULTISIGVERIFY)} OP_IF"
    assert(
      multi == Seq(
        OP_HASH160,
        OP_DUP,
        OP_CHECKMULTISIGVERIFY,
        OP_IF
      ))

    val bytes = hex"deadbeef"
    val withBytes = script"OP_HASH160 $bytes"
    assert(
      withBytes == Seq(OP_HASH160,
                       BytesToPushOntoStack(bytes.length),
                       ScriptConstant(bytes)))

    val pub = ECPublicKey()
    val withNetworkElement =
      script"OP_HASH160 $pub"
    assert(
      withNetworkElement == List(OP_HASH160,
                                 BytesToPushOntoStack(pub.bytes.length),
                                 ScriptConstant(pub.bytes)))
  }

  it must "parse a real script" in {
    val script =
      script"OP_DUP OP_HASH160 e2e7c1ab3f807151e832dd1accb3d4f5d7d19b4b OP_EQUALVERIFY OP_CHECKSIG"
    assert(
      script == Seq(OP_DUP,
                    OP_HASH160,
                    BytesToPushOntoStack(20),
                    ScriptConstant("e2e7c1ab3f807151e832dd1accb3d4f5d7d19b4b"),
                    OP_EQUALVERIFY,
                    OP_CHECKSIG))
  }

  it must "parse scripts with substitutions at the beginning" in {
    val bytes = hex"1234567"
    val script = script"$bytes OP_DUP OP_ELSE"
    assert(
      script == Seq(BytesToPushOntoStack(bytes.length),
                    ScriptConstant(bytes),
                    OP_DUP,
                    OP_ELSE))
  }

  it must "parse scripts with substitutions at the end" in {
    val bytes = hex"1234567"
    val script = script"OP_DUP OP_ELSE $bytes "
    assert(
      script == Seq(OP_DUP,
                    OP_ELSE,
                    BytesToPushOntoStack(bytes.length),
                    ScriptConstant(bytes)))

  }

  it must "abort parsing strings with embedded strings" in {
    assertDoesNotCompile("""
    script" OP_DUP ${"this is a string"} OP_HASH160"
    """)
  }

  it must "abort parsing bad strings" in {
    assertDoesNotCompile("""
    script"foobar
    """")
  }
}
