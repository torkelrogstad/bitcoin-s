package org.bitcoins.node.models
import slick.dbio.Effect.Write
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

abstract class CRUDAutoInc[T <: DbRowAutoInc[T]] extends CRUD[T,Long] {

  /** The table inside our database we are inserting into */
  val table: TableQuery[_ <: TableAutoInc[T]]


  override def createAll(ts: Vector[T]): Future[Vector[T]] = {
    val query = table
      .returning(table.map(_.id))
      .into((t, id) => t.copyWithId(id = id))
    val actions: Vector[DBIOAction[query.SingleInsertResult, NoStream, Write]] =
      ts.map(r => query.+=(r))
    database.runVec(DBIO.sequence(actions))
  }

  override def findByPrimaryKeys(ids: Vector[Long]): Query[Table[_], T, Seq] = {
    table.filter(_.id.inSet(ids))
  }


  override def findAll(ts: Vector[T]): Query[Table[_], T, Seq] = {
    val ids = ts.filter(_.id.isDefined).map(_.id.get)
    findByPrimaryKeys(ids)
  }
}