package org.bitcoins.core.gcs

import org.bitcoins.core.number.{UInt64, UInt8}
import org.bitcoins.core.protocol.blockchain.Block
import org.bitcoins.core.protocol.transaction.{
  Transaction,
  TransactionInput,
  TransactionOutPoint,
  TransactionOutput
}
import org.bitcoins.core.script.control.OP_RETURN
import scodec.bits.{BitVector, ByteVector}

import scala.annotation.tailrec

// TODO: Replace ByteVector with a type for keys
case class GolombFilter(
    key: ByteVector,
    m: UInt64,
    p: UInt8,
    encodedData: BitVector) {
  lazy val decodedHashes: Vector[UInt64] = GCS.golombDecodeSet(encodedData, p)

  lazy val n: Int = decodedHashes.length

  // TODO: Offer alternative that stops decoding when it finds out if data is there
  def matches(data: ByteVector): Boolean = {
    @tailrec
    def binarySearch(
        from: Int,
        to: Int,
        hash: UInt64,
        set: Vector[UInt64]): Boolean = {
      if (to < from) {
        false
      } else {
        val index = (to + from) / 2
        val otherHash = set(index)

        if (hash == otherHash) {
          true
        } else if (hash < otherHash) {
          binarySearch(from, index - 1, hash, set)
        } else {
          binarySearch(index + 1, to, hash, set)
        }
      }
    }

    val f = UInt64(n) * m
    val hash = GCS.hashToRange(data, f, key)

    binarySearch(from = 0, to = n - 1, hash, decodedHashes)
  }
}

object BlockFilter {

  /**
    * Given a Block and access to the UTXO set, constructs a Block Filter
    * for that block as specified in
    * [[https://github.com/bitcoin/bips/blob/master/bip-0158.mediawiki#block-filters]]
    * @param block
    * @param utxoProvider
    * @return
    */
  def apply(block: Block, utxoProvider: TempUtxoProvider): GolombFilter = {
    val key = block.blockHeader.hash.bytes.take(16)

    val transactions: Vector[Transaction] = block.transactions.toVector

    val noCoinbase: Vector[Transaction] = transactions.tail
    val newOutputs: Vector[TransactionOutput] = transactions.flatMap(_.outputs)
    val newScriptPubKeys: Vector[ByteVector] = newOutputs.flatMap { output =>
      if (output.scriptPubKey.asm.contains(OP_RETURN)) {
        None
      } else {
        Some(output.scriptPubKey.asmBytes)
      }
    }

    val inputs: Vector[TransactionInput] = noCoinbase.flatMap(tx => tx.inputs)
    val outpointsSpent: Vector[TransactionOutPoint] = inputs.map { input =>
      input.previousOutput
    }
    val prevOutputs: Vector[TransactionOutput] =
      outpointsSpent.flatMap(utxoProvider.getUtxo)
    val prevOutputScripts: Vector[ByteVector] =
      prevOutputs.map(_.scriptPubKey.asmBytes)

    GCS.buildBasicBlockFilter(prevOutputScripts ++ newScriptPubKeys, key)
  }
}
