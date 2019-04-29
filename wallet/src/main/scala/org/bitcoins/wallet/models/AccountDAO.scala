package org.bitcoins.wallet.models

import org.bitcoins.core.hd.HDCoin
import org.bitcoins.db.{CRUD, SlickUtil}
import org.bitcoins.wallet.db.WalletDbConfig
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

case class AccountDAO(dbConfig: WalletDbConfig)(
    implicit executionContext: ExecutionContext)
    extends CRUD[AccountDb, (HDCoin, Int)] {

  import org.bitcoins.db.DbCommonsColumnMappers._

  override val ec: ExecutionContext = executionContext

  override val table: TableQuery[AccountTable] = TableQuery[AccountTable]

  override def createAll(ts: Vector[AccountDb]): Future[Vector[AccountDb]] =
    SlickUtil.createAllNoAutoInc(ts, database, table)

  override protected def findByPrimaryKeys(
      ids: Vector[(HDCoin, Int)]): Query[Table[_], AccountDb, Seq] = ???

  override def findByPrimaryKey(
      id: (HDCoin, Int)): Query[Table[_], AccountDb, Seq] = {
    val (coin, index) = id
    table
      .filter(_.coin === coin)
      .filter(_.index === index)
  }

  override def findAll(
      accounts: Vector[AccountDb]): Query[Table[_], AccountDb, Seq] =
    findByPrimaryKeys(
      accounts.map(acc => (acc.bip44Account.coin, acc.bip44Account.index)))

  def findAll(): Future[Vector[AccountDb]] = {
    val query = table.result
    database.run(query).map(_.toVector)
  }
}
