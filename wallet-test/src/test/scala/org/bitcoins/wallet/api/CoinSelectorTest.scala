package org.bitcoins.wallet.api

import org.bitcoins.core.currency.{CurrencyUnits, Satoshis}
import org.bitcoins.core.number.Int64
import org.bitcoins.core.protocol.script.ScriptPubKey
import org.bitcoins.core.protocol.transaction.TransactionOutput
import org.bitcoins.core.wallet.fee.{FeeUnit, SatoshisPerByte}
import org.bitcoins.testkit.core.gen.{CryptoGenerators, TransactionGenerators}
import org.bitcoins.wallet.models.UTXOSpendingInfoDb
import org.bitcoins.wallet.util.BitcoinSWalletTest
import org.scalatest.FutureOutcome

class CoinSelectorTest extends BitcoinSWalletTest {
  override type FixtureParam = (
      TransactionOutput,
      FeeUnit,
      UTXOSpendingInfoDb,
      UTXOSpendingInfoDb,
      UTXOSpendingInfoDb)

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val output = TransactionOutput(Satoshis(Int64(99L)), ScriptPubKey.empty)
    val feeRate = SatoshisPerByte(CurrencyUnits.zero)

    val utxo1 = UTXOSpendingInfoDb(
      Some(1),
      TransactionGenerators.outPoint.sample.get,
      TransactionOutput(Satoshis(Int64(10)), ScriptPubKey.empty),
      CryptoGenerators.bip44Path.sample.get,
      None,
      None
    )
    val utxo2 = UTXOSpendingInfoDb(
      Some(2),
      TransactionGenerators.outPoint.sample.get,
      TransactionOutput(Satoshis(Int64(90)), ScriptPubKey.empty),
      CryptoGenerators.bip44Path.sample.get,
      None,
      None
    )
    val utxo3 = UTXOSpendingInfoDb(
      Some(3),
      TransactionGenerators.outPoint.sample.get,
      TransactionOutput(Satoshis(Int64(20)), ScriptPubKey.empty),
      CryptoGenerators.bip44Path.sample.get,
      None,
      None
    )

    test((output, feeRate, utxo1, utxo2, utxo3))
  }

  behavior of "CoinSelector"

  it must "accumulate largest outputs" in { fixture =>
    val (output, feeRate, utxo1, utxo2, utxo3) = fixture

    val selection = CoinSelector.accumulateLargest(Vector(utxo1, utxo2, utxo3),
                                                   Vector(output),
                                                   feeRate)

    assert(selection == Vector(utxo2, utxo3))
  }

  it must "accumulate smallest outputs" in { fixture =>
    val (output, feeRate, utxo1, utxo2, utxo3) = fixture

    val selection =
      CoinSelector.accumulateSmallestViable(Vector(utxo1, utxo2, utxo3),
                                            Vector(output),
                                            feeRate)

    assert(selection == Vector(utxo1, utxo3, utxo2))
  }

  it must "accumulate outputs in order" in { fixture =>
    val (output, feeRate, utxo1, utxo2, utxo3) = fixture

    val selection = CoinSelector.accumulate(Vector(utxo1, utxo2, utxo3),
                                            Vector(output),
                                            feeRate)

    assert(selection == Vector(utxo1, utxo2))
  }
}
