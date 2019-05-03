package org.bitcoins.wallet.models

import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.db.{CRUD, SlickUtil}
import org.bitcoins.wallet.db.WalletDbConfig
import slick.dbio.Effect
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.TableQuery
import slick.sql.SqlAction

import scala.concurrent.{ExecutionContext, Future}
import org.bitcoins.core.hd.{HDChainType, HDPath}

case class AddressDAO[T <: HDPath[T]](override val dbConfig: WalletDbConfig)(
    implicit val ec: ExecutionContext)
    extends CRUD[AddressDb[T], BitcoinAddress] {
  import org.bitcoins.db.DbCommonsColumnMappers._

  override val table: TableQuery[AddressTable[T]] = TableQuery[AddressTable[T]]

  override def createAll(
      ts: Vector[AddressDb[T]]): Future[Vector[AddressDb[T]]] =
    SlickUtil.createAllNoAutoInc(ts, database, table)

  /** Finds the rows that correlate to the given primary keys */
  override def findByPrimaryKeys(
      addresses: Vector[BitcoinAddress]): Query[Table[_], AddressDb[T], Seq] =
    table.filter(_.address.inSet(addresses))

  override def findAll(
      ts: Vector[AddressDb[T]]): Query[Table[_], AddressDb[T], Seq] =
    findByPrimaryKeys(ts.map(_.address))

  def findAddress(addr: BitcoinAddress): Future[Option[AddressDb[T]]] = {
    val query = findByPrimaryKey(addr).result
    database.run(query).map(_.headOption)
  }

  def findAll(): Future[Vector[AddressDb[T]]] = {
    val query = table.result
    database.run(query).map(_.toVector)
  }

  private def addressesForAccountQuery(
      accountIndex: Int): Query[AddressTable[T], AddressDb[T], Seq] =
    table.filter(_.accountIndex === accountIndex)

  def findMostRecentChange(accountIndex: Int): Future[Option[AddressDb[T]]] = {
    val query = findMostRecentForChain(accountIndex, HDChainType.Change)

    database.run(query)
  }

  private def findMostRecentForChain(
      accountIndex: Int,
      chain: HDChainType): SqlAction[
    Option[AddressDb[T]],
    NoStream,
    Effect.Read] = {
    addressesForAccountQuery(accountIndex)
      .filter(_.accountChainType === chain)
      .sortBy(_.addressIndex.desc)
      .take(1)
      .result
      .headOption
  }

  def findMostRecentExternal(
      accountIndex: Int): Future[Option[AddressDb[T]]] = {
    val query = findMostRecentForChain(accountIndex, HDChainType.External)
    database.run(query)
  }
}
