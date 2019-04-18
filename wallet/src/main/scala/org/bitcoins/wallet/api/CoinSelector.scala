package org.bitcoins.wallet.api

import org.bitcoins.core.currency.{CurrencyUnit, CurrencyUnits}
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.wallet.fee.FeeUnit
import org.bitcoins.wallet.models.UTXOSpendingInfoDb

import scala.annotation.tailrec

/** Implements algorithms for selecting from a UTXO set to spend to an output set at a given fee rate. */
trait CoinSelector {

  /**
    * Greedily selects from walletUtxos starting with the largest outputs, skipping outputs with values
    * below their fees. Better for high fee environments than accumulateSmallestViable.
    */
  def accumulateLargest(
      walletUtxos: Vector[UTXOSpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val sortedUtxos =
      walletUtxos.sortBy(_.value.satoshis.toLong).reverse

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /**
    * Greedily selects from walletUtxos starting with the smallest outputs, skipping outputs with values
    * below their fees. Good for low fee environments to consolidate UTXOs.
    *
    * Has the potential privacy breach of connecting a ton of UTXOs to one address.
    */
  def accumulateSmallestViable(
      walletUtxos: Vector[UTXOSpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val sortedUtxos = walletUtxos.sortBy(_.value.satoshis.toLong)

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /** Greedily selects from walletUtxos in order, skipping outputs with values below their fees */
  def accumulate(
      walletUtxos: Vector[UTXOSpendingInfoDb],
      outputs: Vector[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val totalValue = outputs.foldLeft(CurrencyUnits.zero) {
      case (totVal, output) => totVal + output.value
    }

    @tailrec
    def addUtxos(
        alreadyAdded: Vector[UTXOSpendingInfoDb],
        valueSoFar: CurrencyUnit,
        bytesSoFar: Long,
        utxosLeft: Vector[UTXOSpendingInfoDb]): Vector[UTXOSpendingInfoDb] = {
      val fee = feeRate.currencyUnit * bytesSoFar
      if (valueSoFar > totalValue + fee) {
        alreadyAdded
      } else if (utxosLeft.isEmpty) {
        throw new RuntimeException(
          s"Not enough value in given outputs ($valueSoFar) to make transaction spending $totalValue")
      } else {
        val nextUtxo = utxosLeft.head
        val approxUtxoSize = CoinSelector.approximateUtxoSize(nextUtxo)
        val nextUtxoFee = feeRate.currencyUnit * approxUtxoSize
        if (nextUtxo.value < nextUtxoFee) {
          addUtxos(alreadyAdded, valueSoFar, bytesSoFar, utxosLeft.tail)
        } else {
          val newAdded = alreadyAdded.:+(nextUtxo)
          val newValue = valueSoFar + nextUtxo.value

          addUtxos(newAdded,
                   newValue,
                   bytesSoFar + approxUtxoSize,
                   utxosLeft.tail)
        }
      }
    }

    addUtxos(Vector.empty, CurrencyUnits.zero, bytesSoFar = 0L, walletUtxos)
  }
}

object CoinSelector extends CoinSelector {

  /** Cribbed from [[https://github.com/bitcoinjs/coinselect/blob/master/utils.js]] */
  def approximateUtxoSize(utxo: UTXOSpendingInfoDb): Long = {
    val inputBase = 32 + 4 + 1 + 4
    val scriptSize = utxo.redeemScriptOpt match {
      case Some(script) => script.bytes.length
      case None =>
        utxo.scriptWitnessOpt match {
          case Some(script) => script.bytes.length
          case None         => 25 // PUBKEYHASH
        }
    }

    inputBase + scriptSize
  }
}
