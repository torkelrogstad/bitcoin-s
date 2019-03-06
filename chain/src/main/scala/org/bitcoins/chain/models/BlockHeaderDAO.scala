package org.bitcoins.chain.models

import org.bitcoins.chain.config.ChainConfig
import org.bitcoins.core.crypto.DoubleSha256DigestBE
import org.bitcoins.core.protocol.blockchain.ChainParams
import org.bitcoins.db.{CRUD, DbConfig, SlickUtil}
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by chris on 9/8/16.
  * This actor is responsible for all database operations relating to
  * [[BlockHeaderDb]]'s. Currently we store all block headers in a postgresql database
  */
sealed abstract class BlockHeaderDAO
    extends CRUD[BlockHeaderDb, DoubleSha256DigestBE] {

  import ChainColumnMappers._

  def chainParams: ChainParams

  private def genesisBlock: BlockHeaderDb = {
    val blockHeader = chainParams.genesisBlock.blockHeader
    BlockHeaderDbHelper.fromBlockHeader(height = 0, bh = blockHeader)
  }

  override val table: TableQuery[BlockHeaderTable] =
    TableQuery[BlockHeaderTable]

  /** Creates all of the given [[BlockHeaderDb]] in the database */
  override def createAll(
      headers: Vector[BlockHeaderDb]): Future[Vector[BlockHeaderDb]] = {
    val actions = headers.map(t => (table += t).andThen(DBIO.successful(t)))
    val result = database.run(DBIO.sequence(actions))
    result
  }

  override protected def findAll(
      ts: Vector[BlockHeaderDb]): Query[Table[_], BlockHeaderDb, Seq] = {
    findByPrimaryKeys(ts.map(_.hashBE))
  }

  override def findByPrimaryKeys(hashes: Vector[DoubleSha256DigestBE]): Query[
    Table[_],
    BlockHeaderDb,
    Seq] = {
    table.filter(_.hash.inSet(hashes))
  }

  /** Retrieves a [[BlockHeaderDb]] at the given height, if none is found it returns None */
  def getAtHeight(height: Long): Future[Vector[BlockHeaderDb]] = {
    //which would both have height x
    val query = table.filter(_.height === height).result
    database.run(query).map(_.toVector)
  }

  /** Finds the height of the given [[BlockHeaderDb]]'s hash, if it exists */
  def findHeight(hash: DoubleSha256DigestBE): Future[Option[BlockHeaderDb]] = {
    val query = table.filter(_.hash === hash).result
    database.run(query).map(_.headOption)
  }

  /** Returns the maximum block height from our database */
  def maxHeight: Future[Long] = {
    val query = table.map(_.height).max.result
    val result = database.run(query)
    result.map(_.getOrElse(0L))
  }

  /** Returns the chainTips in our database. This can be multiple headers if we have
    * competing blockchains (fork) */
  def chainTips: Future[Vector[BlockHeaderDb]] = {
    val maxF = maxHeight

    maxF.flatMap { maxHeight =>
      getAtHeight(maxHeight)
        .map(_.toVector)
    }
  }
}

object BlockHeaderDAO {
  private case class BlockHeaderDAOImpl(
      chainParams: ChainParams,
      dbConfig: DbConfig)(override implicit val ec: ExecutionContext)
      extends BlockHeaderDAO

  def apply(chainParams: ChainParams, dbConfig: DbConfig)(
      implicit ec: ExecutionContext): BlockHeaderDAO = {
    BlockHeaderDAOImpl(chainParams, dbConfig)(ec)
  }

}
