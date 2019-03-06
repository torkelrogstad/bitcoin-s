package org.bitcoins.core.protocol.psbt

import org.bitcoins.core.crypto.{ECDigitalSignature, ECPublicKey}
import org.bitcoins.core.currency.Bitcoins
import org.bitcoins.core.protocol.psbt.Psbt.PsbtGlobalData
import org.bitcoins.core.protocol.script.{
  ScriptPubKey,
  ScriptSignature,
  ScriptWitness,
  WitnessScriptPubKey
}
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.core.protocol.{CompactSizeUInt, NetworkElement}
import org.bitcoins.core.script.crypto.HashType
import org.bitcoins.core.util.Factory
import scodec.bits.{ByteVector, HexStringSyntax}

import scala.annotation.tailrec

/**
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0174.mediawiki BIP174]]
  */
sealed abstract class Psbt extends NetworkElement {

  def globalData: PsbtGlobalData

  def inputs: Vector[Vector[PsbtInput]]
  def outputs: Vector[PsbtOutput]

  def fee: Bitcoins

  /** The byte representation of the PSBT */
  override def bytes: ByteVector = hex""
}

object Psbt extends Factory[Psbt] {

  private case class PsbtImpl(
      globalData: PsbtGlobalData,
      inputs: Vector[Vector[PsbtInput]],
      outputs: Vector[PsbtOutput],
      fee: Bitcoins
  ) extends Psbt

  type UnknownData = Map[ByteVector, ByteVector]

  val KEY_VALUE_MAP_SEP: ByteVector = hex"0x00"

  /**
    * Magic bytes which are ASCII for `psbt`
    */
  val MAGIC_BYTES: ByteVector = hex"0x70736274"
  val MAGIC_BYTES_SEP: ByteVector = hex"0xff"
  val HEADER: ByteVector = MAGIC_BYTES ++ MAGIC_BYTES_SEP
  val HEADER_LENGTH: Int = 5

  val PSBT_MIN_LENGTH = 8

  /** Creates a PSBT out of a sequence of bytes. */
  override def fromBytes(bytes: ByteVector): Psbt = {
    require(
      bytes.length >= PSBT_MIN_LENGTH,
      s"PSBTs must be longer than $PSBT_MIN_LENGTH bytes! Found: ${bytes.length}")

    val (header, data) = bytes.splitAt(HEADER_LENGTH)
    require(
      header == HEADER,
      s"First $HEADER_LENGTH bytes were not ${HEADER.toHex}! Found: ${header.toHex}")

    val (globalData, inputsAndOutputsBytes) = processGlobalData(data)

    val inputsAmount = globalData.unsignedTx.inputs.length
    val (inputs, outputsBytes) =
      processInputs(inputsAmount, inputsAndOutputsBytes)

    assert(globalData.unsignedTx.inputs.forall { i =>
      true // todo: verify that all tx inputs have a matching input map
    })

    val outputs = processOutputs(outputsBytes)
    assert(globalData.unsignedTx.outputs.forall { o =>
      true // todo: verify that all tx outputs have a matching output map
    })

    PsbtImpl(globalData, inputs, outputs, Bitcoins.zero)
  }

  case class PsbtGlobalData(unsignedTx: Transaction, unknownData: UnknownData)

  private def makePsbtInput(keyValuePair: KeyValuePair): PsbtInput = {
    import PsbtInputTypes._
    val KeyValuePair(key, value) = keyValuePair
    key match {
      case PSBT_IN_NON_WITNESS_UTXO =>
        PsbtInputNonWitnessUtxo(Transaction.fromBytes(value))
      case PSBT_IN_WITNESS_UTXO =>
        PsbtInputWitnessUtxo(Transaction.fromBytes(value))
      case PSBT_IN_PARTIAL_SIG =>
        throw new IllegalArgumentException(
          "Key PSBT_IN_PARTIAL_SIG must include a public key")
      case PSBT_IN_PARTIAL_SIG +: rest =>
        val pubkey = ECPublicKey.fromBytes(rest)
        val signature = ECDigitalSignature.fromBytes(value)
        PsbtInputPartialSignature(pubkey, signature)
      case PSBT_IN_SIGHASH_TYPE =>
        PsbtInputSighashType(HashType.fromBytes(value))
      case PSBT_IN_REDEEM_SCRIPT =>
        PsbtInputRedeemScript(ScriptPubKey.fromBytes(value))
      case PSBT_IN_WITNESS_SCRIPT =>
        PsbtInputWitnessScript(???)
      case PSBT_IN_BIP32_DERIVATION =>
        throw new IllegalArgumentException(
          "Key PSBT_IN_BIP32_DERIVATION must include a public key")
      case PSBT_IN_BIP32_DERIVATION +: rest =>
        val pubkey = ECPublicKey.fromBytes(rest)
        // master key fingerprint
        ???
      case PSBT_IN_FINAL_SCRIPTSIG =>
        PsbtInputFinalScriptSig(ScriptSignature.fromBytes(value))
      case PSBT_IN_FINAL_SCRIPTWITNESS =>
        PsbtInputFinalScriptWitnesss(???)
      case unknown: ByteVector =>
        PsbtInputUnknown(key = unknown, value = value)
    }
  }

  /**
    * Extracts the inputs of the unsigned transaction, and returns
    * the remainding bytes.
    */

  @tailrec
  def processInputs(
      inputsLeft: Int,
      bytes: ByteVector,
      accum: Vector[Vector[PsbtInput]] = Vector.empty): (
      Vector[Vector[PsbtInput]],
      ByteVector) = {
    if (inputsLeft == 0) (accum, bytes)
    else {
      val (keyValuePairs, remaindingBytes) = processKeyValueMap(bytes)
      val inputs: Vector[PsbtInput] = keyValuePairs.map(makePsbtInput)
      processInputs(inputsLeft = inputsLeft - 1,
                    bytes = remaindingBytes,
                    accum = accum :+ inputs)
    }

  }

  /**
    * Extracts the outputs of the unsigned transaction.
    */
  private def processOutputs(bytes: ByteVector): Vector[PsbtOutput] =
    Vector.empty // todo

  /**
    * Extracts the unsigned transaction, and returns the remainding bytes.
    *
    * <br></br>
    * <br></br>
    *
    * __Key:__ The key must only contain the 1 byte type: {0x00}
    *
    * <br></br>
    * <br></br>
    *
    * __Value:__ The transaction in network serialization. The scriptSigs
    * and witnesses for each input must be empty. The transaction must
    * be in the old serialization format (without witnesses). A PSBT
    * must have a transaction, otherwise it is invalid.
    */
  private def processGlobalData(
      bytes: ByteVector): (PsbtGlobalData, ByteVector) = {
    val (keyValuePairs, remaindingBytes) = processKeyValueMap(bytes)

    val (unsignedTxPairVec, unknownPairs) = keyValuePairs.partition(pair =>
      pair.key == PsbtGlobalTypes.PSBT_GLOBAL_UNSIGNED_TX)

    assert(
      unsignedTxPairVec.nonEmpty,
      s"Did not find key-value pair for the unsigned transaction! Expected key: ${PsbtGlobalTypes.PSBT_GLOBAL_UNSIGNED_TX.toHex}, found keys: ${unknownPairs
        .map(_.key.toHex)
        .mkString(",")}"
    )

    // since we do a uniqueness check above, we are certain
    // unisngedTxPairVec has length 1
    val unsignedTxPair +: _ = unsignedTxPairVec

    val unsignedTx = Transaction.fromBytes(unsignedTxPair.value)

    val unknownData: UnknownData = unknownPairs.map {
      case KeyValuePair(key, value) => key -> value
    }.toMap

    val globalData =
      PsbtGlobalData(unsignedTx = unsignedTx, unknownData = unknownData)

    (globalData, remaindingBytes)
  }

  private case class KeyValuePair(key: ByteVector, value: ByteVector)

  /**
    * Recurses over the provided bytes until it encounters the expected
    * separator bytes.
    *
    * @return a vector of the found key-value pairs and the remainding bytes
    */
  private def processKeyValueMap(
      bytes: ByteVector): (Vector[KeyValuePair], ByteVector) = {

    @tailrec
    def helper(bytes: ByteVector, accum: Vector[KeyValuePair] = Vector.empty): (
        Vector[KeyValuePair],
        ByteVector) = {
      val (keyValuePair, remainding) = processKeyValuePair(bytes)
      val newAccum = accum :+ keyValuePair
      if (remainding.startsWith(KEY_VALUE_MAP_SEP)) {
        (newAccum, remainding.drop(KEY_VALUE_MAP_SEP.length))
      } else {
        helper(remainding, newAccum)
      }
    }
    val (pairs, remainder) = helper(bytes)

    // used in error message
    lazy val keys = pairs.map(_.key.toHex).mkString(", ")

    pairs.ensuring(
      keyValuePairs => {
        def isKeyUnique(keyValuePair: KeyValuePair) =
          keyValuePairs.count(_.key == keyValuePair.key) <= 1

        keyValuePairs.forall(isKeyUnique)
      },
      s"All keys in a key-value map must be unique! Keys found: $keys"
    )

    (pairs, remainder)
  }

  /**
    * Extracts a key value pair from the beginning of the bytevector, and
    * returns the pair and the remainding bytes.
    */
  private def processKeyValuePair(
      bytes: ByteVector): (KeyValuePair, ByteVector) = {
    val keyLength = CompactSizeUInt.parseCompactSizeUInt(bytes)
    keyLength.bytes.length

    val keyPos = keyLength.bytes.length
    val key = bytes.slice(keyPos, keyPos + keyLength.toInt)

    val valueLengthPos = keyPos + keyLength.toInt
    val valueLength =
      CompactSizeUInt.parseCompactSizeUInt(bytes.drop(valueLengthPos))

    val valuePos = valueLengthPos + valueLength.bytes.length
    val value = bytes.slice(valuePos, valuePos + valueLength.toInt)

    val remaindingBytes = bytes.drop(valuePos + valueLength.toInt)

    (KeyValuePair(key, value), remaindingBytes)

  }
}

object PsbtGlobalTypes {
  // followed by transaction, network serialization
  val PSBT_GLOBAL_UNSIGNED_TX: ByteVector = hex"0x00"
}
