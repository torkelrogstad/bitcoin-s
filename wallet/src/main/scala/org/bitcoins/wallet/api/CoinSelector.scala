package org.bitcoins.wallet.api

import org.bitcoins.core.currency.{CurrencyUnit, CurrencyUnits}
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.wallet.fee.FeeUnit
import org.bitcoins.wallet.models.UTXOSpendingInfoDb

import scala.annotation.tailrec

/** Implements algorithms for selecting from a UTXO set to spend to an output set at a given fee rate. */
trait CoinSelector {

  /** Greedily selects from walletUtxos starting with the largest outputs, skipping detrimental outputs */
  def accumulateLargest(
      walletUtxos: Seq[UTXOSpendingInfoDb],
      outputs: Seq[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val sortedUtxos =
      walletUtxos.toVector.sortBy(_.value.satoshis.toLong).reverse

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /** Greedily selects from walletUtxos starting with the smallest outputs, skipping detrimental outputs */
  def accumulateSmallestViable(
      walletUtxos: Seq[UTXOSpendingInfoDb],
      outputs: Seq[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val sortedUtxos = walletUtxos.toVector.sortBy(_.value.satoshis.toLong)

    accumulate(sortedUtxos, outputs, feeRate)
  }

  /** Greedily selects from walletUtxos in order, skipping detrimental outputs */
  def accumulate(
      walletUtxos: Seq[UTXOSpendingInfoDb],
      outputs: Seq[TransactionOutput],
      feeRate: FeeUnit): Vector[UTXOSpendingInfoDb] = {
    val totalValue = outputs.foldLeft(CurrencyUnits.zero) {
      case (totVal, output) => totVal + output.value
    }

    @tailrec
    def addUtxos(
        alreadyAdded: Vector[UTXOSpendingInfoDb],
        valueSoFar: CurrencyUnit,
        utxosLeft: Vector[UTXOSpendingInfoDb]): Vector[UTXOSpendingInfoDb] = {
      val fee = CurrencyUnits.zero // TODO: Calculate this
      if (valueSoFar > totalValue + fee) {
        alreadyAdded
      } else {
        val nextUtxo = utxosLeft.head
        val nextUtxoFee = CurrencyUnits.zero // TODO: Calculate this
        if (nextUtxo.value < nextUtxoFee) {
          addUtxos(alreadyAdded, valueSoFar, utxosLeft.tail)
        } else {
          val newAdded = alreadyAdded.:+(nextUtxo)
          val newValue = valueSoFar + nextUtxo.value

          addUtxos(newAdded, newValue, utxosLeft.tail)
        }
      }
    }

    addUtxos(Vector.empty, CurrencyUnits.zero, walletUtxos.toVector)
  }
}

object CoinSelector extends CoinSelector
