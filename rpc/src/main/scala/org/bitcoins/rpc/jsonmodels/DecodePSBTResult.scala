package org.bitcoins.rpc.jsonmodels

sealed abstract class DecodePSBTResult

case class RpcPSBT(
  tx: RpcTransaction,
  // unknown: RpcUnknown,
  inputs: Seq[PSBTInput],
  // outputs: Seq[RpcPSBTOutput],
  fee: Option[Float]) extends DecodePSBTResult

case class PSBTInput(
  non_witness_utxo: Option[RpcTransaction],
  partial_signatures: Option[Map[String, String]],
  final_scriptSig: Option[PSBTScript],
  witness_utxo: Option[WitnessUTXO],
  redeem_script: Option[PSBTScriptWithType],
  witness_script: Option[PSBTScriptWithType],
  final_scriptwitness: Seq[String],
  bip32_derivs: Option[PSBTBip32Derivs],
  sighash: Option[String]) extends DecodePSBTResult

case class PSBTScriptWithType(
  asm: String,
  hex: String,
  scriptType: String) extends DecodePSBTResult

case class PSBTHashType() extends DecodePSBTResult

case class PSBTScript(
  asm: String,
  hex: String) extends DecodePSBTResult

case class WitnessUTXO(
  amount: Float,
  scriptPubKey: PSBTScriptPubKey) extends DecodePSBTResult

case class PSBTBip32Derivs(
  pubKey: Option[PSBTBip32DerivsPubKey]) extends DecodePSBTResult

case class PSBTBip32DerivsPubKey(
  master_fingerprint: String,
  path: String) extends DecodePSBTResult

case class PSBTScriptPubKey(
  asm: String,
  hex: String,
  scriptType: String,
  address: String) extends DecodePSBTResult


