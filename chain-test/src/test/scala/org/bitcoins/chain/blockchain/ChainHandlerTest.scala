package org.bitcoins.chain.blockchain

import akka.actor.ActorSystem
import org.bitcoins.chain.models.{BlockHeaderDb, BlockHeaderDbHelper}
import org.bitcoins.chain.util.ChainUnitTest
import org.bitcoins.core.protocol.blockchain.{
  BlockHeader,
  ChainParams,
  MainNetChainParams
}
import org.bitcoins.testkit.chain.{BlockHeaderHelper, ChainTestUtil}
import org.scalatest.FutureOutcome
import play.api.libs.json.Json

import scala.concurrent.Future

class ChainHandlerTest extends ChainUnitTest {

  override type FixtureParam = ChainHandler

  override implicit val system = ActorSystem("ChainUnitTest")

  override val defaultTag: FixtureTag = FixtureTag.GenisisChainHandler

  override lazy val defaultChainParam: ChainParams = MainNetChainParams

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withChainHandler(test)

  behavior of "ChainHandler"

  it must "process a new valid block header, and then be able to fetch that header" in {
    chainHandler: ChainHandler =>
      val newValidHeader = BlockHeaderHelper.buildNextHeader(genesisHeaderDb)
      val processedHeaderF =
        chainHandler.processHeader(newValidHeader.blockHeader)

      val foundHeaderF =
        processedHeaderF.flatMap(_.getHeader(newValidHeader.hashBE))

      foundHeaderF.map(found => assert(found.get == newValidHeader))
  }

  it must "be able to process and fetch real headers from mainnet" in {
    chainHandler: ChainHandler =>
      val source =
        scala.io.Source.fromURL(getClass.getResource("/block_headers.json"))
      val arrStr = source.getLines.next
      source.close()

      import org.bitcoins.rpc.serializers.JsonReaders.BlockHeaderReads
      val headersResult = Json.parse(arrStr).validate[Vector[BlockHeader]]
      if (headersResult.isError) {
        fail(headersResult.toString)
      }

      val blockHeaders = headersResult.get
      val firstBlockHeaderDb =
        BlockHeaderDbHelper.fromBlockHeader(FIRST_BLOCK_HEIGHT,
                                            ChainTestUtil.blockHeader562375)

      chainHandler.blockchain.blockHeaderDAO
        .create(firstBlockHeaderDb)
        .flatMap { _ =>
          var processorF = Future.successful(chainHandler.copy(blockchain =
            chainHandler.blockchain.copy(headers = Vector(firstBlockHeaderDb))))
          var blockCount = FIRST_BLOCK_HEIGHT

          val processFVec = blockHeaders.tail.map { blockHeader =>
            processorF = processorF.flatMap { processor =>
              processor.processHeader(blockHeader)
            }

            processorF.flatMap { processor =>
              val headerFoundF = processor.getHeader(blockHeader.hashBE).map {
                headerOpt =>
                  assert(headerOpt.contains(blockHeader))
              }

              val sizeChangedF = processor.getBlockCount.map { count =>
                blockCount = blockCount + 1
                assert(count == blockCount)
              }

              headerFoundF.flatMap(_ => sizeChangedF)
            }
          }

          Future.sequence(processFVec).map(_ => succeed)
        }
  }

}
