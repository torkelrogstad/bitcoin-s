package org.bitcoins.wallet.models

import org.bitcoins.db.CRUDAutoInc
import org.bitcoins.wallet.db.WalletDbConfig
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

case class UTXOSpendingInfoDAO(dbConfig: WalletDbConfig)(
    implicit val ec: ExecutionContext)
    extends CRUDAutoInc[UTXOSpendingInfoDb[_]] {

  /** The table inside our database we are inserting into */
  override val table = TableQuery[UTXOSpendingInfoTable]

  def findAllUTXOs(): Future[Vector[UTXOSpendingInfoDb[_]]] =
    database.run(table.result).map(_.toVector)
}
