package org.bitcoins.core.protocol.psbt

import org.bitcoins.core.crypto.ECPublicKey
import org.bitcoins.core.protocol.script.{ScriptPubKey, WitnessScriptPubKey}
import scodec.bits.ByteVector
import scodec.bits.HexStringSyntax

/**
  * Represents a input in a [[org.bitcoins.core.protocol.psbt.Psbt PSBT]]
  */
sealed abstract class PsbtOutput

final case class PsbtOutputRedeemScript(redeemScript: ScriptPubKey)
    extends PsbtOutput

final case class PsbtOutputWitnessScript(witnessScript: WitnessScriptPubKey)
    extends PsbtOutput

final case class PsbtOutputBi32Derivation(
    publicKey: ECPublicKey,
    fingerprint: ByteVector,
    derivationPath: ByteVector)

/**
  * The various (known) values a key in a PSBT input can have
  */
object PsbtOutputTypes {

  /** The `redeemScript` for this output if it has one */
  val PSBT_OUT_REDEEM_SCRIPT: ByteVector = hex"0x00"

  /** The `witnessScript` for this output if it has one */
  val PSBT_OUT_WITNESS_SCRIPT: ByteVector = hex"0x01"

  /**
    * The master key fingerprint concatenated with the
    * derivation path of the public key. The derivation
    * path is represented as 32 bit unsigned integer
    * indexes concatenated with each other. This must omit
    * the index of the master key. Public keys are those
    * needed to spend this output.
    */
  val PSBT_OUT_BIP32_DERIVATION: ByteVector = hex"0x02"
}
