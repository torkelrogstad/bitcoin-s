package org.bitcoins.wallet.models

import org.bitcoins.core.hd.HDPath
import org.bitcoins.db.CRUDAutoInc
import org.bitcoins.wallet.db.WalletDbConfig
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

case class UTXOSpendingInfoDAO[T <: HDPath[T]](dbConfig: WalletDbConfig)(
    implicit val ec: ExecutionContext)
    extends CRUDAutoInc[UTXOSpendingInfoDb[T]] {

  /** The table inside our database we are inserting into */
  override val table = ??? //TableQuery[UTXOSpendingInfoTable[T]]

  def findAllUTXOs(): Future[Vector[UTXOSpendingInfoDb[_]]] =
    ??? //database.run(table.result).map(_.toVector)
}
