package org.bitcoins.node

import org.bitcoins.core.protocol.blockchain.TestNetChainParams
import org.bitcoins.node.constant.Constants
import org.bitcoins.node.networking.sync.BlockHeaderSyncActor
import org.bitcoins.node.networking.sync.BlockHeaderSyncActor.StartAtLastSavedHeader
import org.bitcoins.node.constant.Constants
import org.bitcoins.node.models.BlockHeaderTable
import org.bitcoins.node.networking.sync.BlockHeaderSyncActor
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
  * Created by chris on 8/29/16.
  */
object Main extends App {

  override def main(args: Array[String]) = {
    /*    val table = TableQuery[BlockHeaderTable]
    val db = Constants.database
    Await.result(Constants.database.run(table.schema.create),3.seconds)
    db.close()*/

    /*
    val gensisBlockHash = TestNetChainParams.genesisBlock.blockHeader.hash
    val startHeader = BlockHeaderSyncActor.StartHeaders(Seq(gensisBlockHash))

    Constants.database.executor*/
    val blockHeaderSyncActor = BlockHeaderSyncActor(Constants.actorSystem,
                                                    Constants.dbConfig,
                                                    Constants.networkParameters)
    blockHeaderSyncActor ! StartAtLastSavedHeader
  }

}
