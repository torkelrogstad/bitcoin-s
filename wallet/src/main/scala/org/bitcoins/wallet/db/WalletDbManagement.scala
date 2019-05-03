package org.bitcoins.wallet.db

import org.bitcoins.core.hd.SegWitHDPath
import org.bitcoins.db.DbManagement
import org.bitcoins.wallet.models.{
  AccountTable,
  AddressTable,
  MnemonicCodeTable,
  UTXOSpendingInfoTable
}
import slick.jdbc.SQLiteProfile.api._

sealed abstract class WalletDbManagement extends DbManagement {
  private val accountTable = TableQuery[AccountTable]
  private val segWitAddressTable = TableQuery[AddressTable[SegWitHDPath]]
  private val mnemonicDAO = TableQuery[MnemonicCodeTable]
  private val segWitUtxoDAO = TableQuery[UTXOSpendingInfoTable[SegWitHDPath]]

  override val allTables: List[TableQuery[_ <: Table[_]]] =
    List(accountTable, segWitAddressTable, mnemonicDAO, segWitUtxoDAO)

}

object WalletDbManagement extends WalletDbManagement
