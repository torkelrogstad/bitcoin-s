package org.bitcoins.chain.models

import org.bitcoins.core.crypto.DoubleSha256DigestBE
import org.bitcoins.db.{AppConfig, CRUD, DbConfig, SlickUtil}
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

/**
  * This class is responsible for all database access related
  * to [[org.bitcoins.core.protocol.blockchain.BlockHeader]]s in
  * our chain project
  */
sealed abstract class BlockHeaderDAO
    extends CRUD[BlockHeaderDb, DoubleSha256DigestBE] {

  import org.bitcoins.db.DbCommonsColumnMappers._

  def appConfig: AppConfig

  override def dbConfig: DbConfig = appConfig.dbConfig

  override val table: TableQuery[BlockHeaderTable] =
    TableQuery[BlockHeaderTable]

  /** Creates all of the given [[BlockHeaderDb]] in the database */
  override def createAll(
      headers: Vector[BlockHeaderDb]): Future[Vector[BlockHeaderDb]] = {
    SlickUtil.createAllNoAutoInc(ts = headers,
                                 database = database,
                                 table = table)
  }

  override protected def findAll(
      ts: Vector[BlockHeaderDb]): Query[Table[_], BlockHeaderDb, Seq] = {
    findByPrimaryKeys(ts.map(_.hashBE))
  }

  def findByHash(hash: DoubleSha256DigestBE): Future[Option[BlockHeaderDb]] = {
    val query = findByPrimaryKey(hash).result
    database.runVec(query).map(_.headOption)
  }

  override def findByPrimaryKeys(hashes: Vector[DoubleSha256DigestBE]): Query[
    Table[_],
    BlockHeaderDb,
    Seq] = {
    table.filter(_.hash.inSet(hashes))
  }

  /** Retrives the ancestor for the given block header at the given height
    * @param child
    * @param height
    * @return
    */
  def getAncestorAtHeight(
      child: BlockHeaderDb,
      height: Long): Future[Option[BlockHeaderDb]] = {
    val headersF = getBetweenHeights(from = height, to = child.height - 1)

    val headersByHeight: Array[Vector[BlockHeaderDb]] =
      new Array[Vector[BlockHeaderDb]](_length = (child.height - height).toInt)
    headersByHeight.indices.foreach(headersByHeight(_) = Vector.empty)

    headersF.map { headers =>
      headers.foreach { header =>
        val index = (header.height - height).toInt
        headersByHeight(index) = headersByHeight(index).:+(header)
      }

      val groupedByHeightHeaders: Vector[Vector[BlockHeaderDb]] =
        headersByHeight.toVector

      @tailrec
      def loop(
          currentHeader: BlockHeaderDb,
          headersByDescHeight: Vector[Vector[BlockHeaderDb]]): Option[
        BlockHeaderDb] = {
        if (currentHeader.height == height) {
          Some(currentHeader)
        } else {
          val prevHeaderOpt = headersByDescHeight.headOption.flatMap(
            _.find(_.hashBE == currentHeader.previousBlockHashBE))

          prevHeaderOpt match {
            case None             => None
            case Some(prevHeader) => loop(prevHeader, headersByDescHeight.tail)
          }
        }
      }

      loop(child, groupedByHeightHeaders.reverse)
    }
  }

  /** Retrieves a [[BlockHeaderDb]] at the given height */
  def getAtHeight(height: Long): Future[Vector[BlockHeaderDb]] = {
    val query = getAtHeightQuery(height)
    database.runVec(query)
  }

  def getAtHeightQuery(height: Long): SQLiteProfile.StreamingProfileAction[
    Seq[BlockHeaderDb],
    BlockHeaderDb,
    Effect.Read] = {
    table.filter(_.height === height).result
  }

  def getBetweenHeights(from: Long, to: Long): Future[Vector[BlockHeaderDb]] = {
    val query = getBetweenHeightsQuery(from, to)
    database.runVec(query)
  }

  def getBetweenHeightsQuery(
      from: Long,
      to: Long): SQLiteProfile.StreamingProfileAction[
    Seq[BlockHeaderDb],
    BlockHeaderDb,
    Effect.Read] = {
    table.filter(header => header.height >= from && header.height <= to).result
  }

  /** Returns the maximum block height from our database */
  def maxHeight: Future[Long] = {
    val query = maxHeightQuery
    val result = database.run(query)
    result
  }

  private def maxHeightQuery: SQLiteProfile.ProfileAction[
    Long,
    SQLiteProfile.api.NoStream,
    Effect.Read] = {
    val query = table.map(_.height).max.getOrElse(0L).result
    query
  }

  /** Returns the chainTips in our database. This can be multiple headers if we have
    * competing blockchains (fork) */
  def chainTips: Future[Vector[BlockHeaderDb]] = {
    val aggregate = {
      maxHeightQuery.flatMap { height =>
        getAtHeightQuery(height)
      }
    }

    database.runVec(aggregate)
  }
}

object BlockHeaderDAO {
  private case class BlockHeaderDAOImpl(appConfig: AppConfig)(
      override implicit val ec: ExecutionContext)
      extends BlockHeaderDAO

  def apply(appConfig: AppConfig)(
      implicit ec: ExecutionContext): BlockHeaderDAO = {
    BlockHeaderDAOImpl(appConfig)(ec)
  }

}
