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

    addUtxos(Vector.empty, CurrencyUnits.zero, walletUtxos)
  }
}

object CoinSelector extends CoinSelector
