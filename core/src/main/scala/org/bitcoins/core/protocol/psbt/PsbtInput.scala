package org.bitcoins.core.protocol.psbt

import org.bitcoins.core.crypto.{ECDigitalSignature, ECPublicKey}
import org.bitcoins.core.protocol.script.{
  ScriptPubKey,
  ScriptSignature,
  ScriptWitness,
  WitnessScriptPubKey
}
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.core.script.crypto.HashType
import scodec.bits.ByteVector
import scodec.bits.HexStringSyntax

/**
  * Represents a input in a [[org.bitcoins.core.protocol.psbt.Psbt PSBT]]
  */
sealed abstract class PsbtInput

final case class PsbtInputNonWitnessUtxo(transaction: Transaction)
    extends PsbtInput

final case class PsbtInputWitnessUtxo(transaction: Transaction)
    extends PsbtInput

final case class PsbtInputPartialSignature(
    publicKey: ECPublicKey,
    signature: ECDigitalSignature)
    extends PsbtInput

final case class PsbtInputSighashType(hashtype: HashType) extends PsbtInput

final case class PsbtInputRedeemScript(redeemScript: ScriptPubKey)
    extends PsbtInput

final case class PsbtInputWitnessScript(witnessScript: WitnessScriptPubKey)
    extends PsbtInput

final case class PsbtInputBip32Derivation(
    publicKey: ECPublicKey,
    fingerprint: ByteVector,
    derivationPath: ByteVector)
    extends PsbtInput

final case class PsbtInputFinalScriptSig(scriptSig: ScriptSignature)
    extends PsbtInput

final case class PsbtInputFinalScriptWitnesss(scriptWitnesss: ScriptWitness)
    extends PsbtInput

final case class PsbtInputUnknown(key: ByteVector, value: ByteVector)
    extends PsbtInput

/**
  * The various (known) values a key in a PSBT input can have
  */
object PsbtInputTypes {

  /**
    * The transaction in network serialization format the current
    * input spends from. This should only be present for inputs
    * which spend non-segwit outputs. However, if it is unknown
    * whether an input spends a segwit output, this type should
    * be used.
    */
  val PSBT_IN_NON_WITNESS_UTXO: ByteVector = hex"0x00"

  /**
    * The entire transaction output in network serialization
    * which the current input spends from. This should only
    * be present for inputs which spend segwit outputs,
    * including P2SH embedded ones.
    */
  val PSBT_IN_WITNESS_UTXO: ByteVector = hex"0x01"

  val PSBT_IN_PARTIAL_SIG: ByteVector = hex"0x02"

  val PSBT_IN_SIGHASH_TYPE: ByteVector = hex"0x03"

  val PSBT_IN_REDEEM_SCRIPT: ByteVector = hex"0x04"

  val PSBT_IN_WITNESS_SCRIPT: ByteVector = hex"0x05"

  val PSBT_IN_BIP32_DERIVATION: ByteVector = hex"0x06"

  val PSBT_IN_FINAL_SCRIPTSIG: ByteVector = hex"0x07"

  val PSBT_IN_FINAL_SCRIPTWITNESS: ByteVector = hex"0x08"
}
