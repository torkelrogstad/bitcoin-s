package org.bitcoins.core.protocol.psbt

import org.bitcoins.core.currency.Bitcoins
import org.bitcoins.core.protocol.psbt.Psbt.PsbtGlobalData
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.core.protocol.{CompactSizeUInt, NetworkElement}
import org.bitcoins.core.util.Factory
import scodec.bits.{ByteVector, HexStringSyntax}

import scala.annotation.tailrec

/**
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0174.mediawiki BIP174]]
  */
sealed abstract class Psbt extends NetworkElement {

  def globalData: PsbtGlobalData

  def inputs: Vector[PsbtInput]
  def outputs: Vector[PsbtOutput]

  def fee: Bitcoins

  /** The byte representation of the PSBT */
  override def bytes: ByteVector = hex""
}

object Psbt extends Factory[Psbt] {

  private case class PsbtImpl(
      globalData: PsbtGlobalData,
      inputs: Vector[PsbtInput],
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

    val (inputs, outputsBytes) = processInputs(inputsAndOutputsBytes)
    assert(globalData.unsignedTx.inputs.forall { i =>
      true // todo: verify that all tx inputs have a matching input map
    })

    val outputs = processOutputs(outputsBytes)
    assert(globalData.unsignedTx.outputs.forall { o =>
      true // todo: verify that all tx outputs have a matching output map
    })

    PsbtImpl(globalData = globalData,
             inputs = inputs,
             outputs = outputs,
             fee = Bitcoins.zero)
  }

  case class PsbtGlobalData(unsignedTx: Transaction, unknownData: UnknownData)

  /**
    * Extracts the inputs of the unsigned transaction, and returns
    * the remainding bytes.
    */
  private def processInputs(
      bytes: ByteVector): (Vector[PsbtInput], ByteVector) = {
    val (keyValuePairs, remaindingBytes) = processKeyValueMap(bytes)
    val inputs: Vector[PsbtInput] = keyValuePairs.map {
      case KeyValuePair(key, value) =>
        key match {
          case PsbtInputTypes.PSBT_IN_NON_WITNESS_UTXO =>
            PsbtInputNonWitnessUtxo(Transaction.fromBytes(value))
          case _: ByteVector => ???
        }
    }
    (inputs, remaindingBytes)
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

sealed abstract class PsbtInput
case class PsbtInputNonWitnessUtxo(transaction: Transaction) extends PsbtInput

sealed abstract class PsbtOutput

object PsbtGlobalTypes {
  // followed by transaction, network serialization
  val PSBT_GLOBAL_UNSIGNED_TX: ByteVector = hex"0x00"
}

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
