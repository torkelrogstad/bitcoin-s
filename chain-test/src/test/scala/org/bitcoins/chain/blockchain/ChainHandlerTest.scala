package org.bitcoins.chain.blockchain

import akka.actor.ActorSystem
import org.bitcoins.chain.models.{BlockHeaderDb, BlockHeaderDbHelper}
import org.bitcoins.chain.util.ChainUnitTest
import org.bitcoins.core.protocol.blockchain.{
  BlockHeader,
  ChainParams,
  MainNetChainParams
}
import org.bitcoins.core.util.FileUtil
import org.bitcoins.testkit.chain.{BlockHeaderHelper, ChainTestUtil}
import org.scalatest.FutureOutcome
import play.api.libs.json.Json

import scala.concurrent.Future

class ChainHandlerTest extends ChainUnitTest {

  override type FixtureParam = ChainHandler

  override implicit val system = ActorSystem("ChainUnitTest")

  override val defaultTag: FixtureTag = FixtureTag.GenisisChainHandler

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

  it must "have an in-order seed" in { _ =>
    val source = FileUtil.getFileAsSource("block_headers.json")
    val arrStr = source.getLines.next
    source.close()

    import org.bitcoins.rpc.serializers.JsonReaders.BlockHeaderReads
    val headersResult = Json.parse(arrStr).validate[Vector[BlockHeader]]
    if (headersResult.isError) {
      fail(headersResult.toString)
    }

    val blockHeaders = headersResult.get

    blockHeaders.reduce[BlockHeader] {
      case (prev, next) =>
        assert(next.previousBlockHashBE == prev.hashBE)
        next
    }

    succeed
  }

  it must "be able to process and fetch real headers from mainnet" in {
    chainHandler: ChainHandler =>
      val source = FileUtil.getFileAsSource("block_headers.json")
      val arrStr = source.getLines.next
      source.close()

      import org.bitcoins.rpc.serializers.JsonReaders.BlockHeaderReads
      val headersResult = Json.parse(arrStr).validate[Vector[BlockHeader]]
      if (headersResult.isError) {
        fail(headersResult.toString)
      }

      val firstPowChange = (FIRST_BLOCK_HEIGHT / chainHandler.chainParams.difficultyChangeInterval + 1) * chainHandler.chainParams.difficultyChangeInterval

      val blockHeaders =
        headersResult.get.drop((firstPowChange - FIRST_BLOCK_HEIGHT).toInt)

      val firstBlockHeaderDb =
        BlockHeaderDbHelper.fromBlockHeader(firstPowChange - 2,
                                            ChainTestUtil.blockHeader562462)

      val secondBlockHeaderDb =
        BlockHeaderDbHelper.fromBlockHeader(firstPowChange - 1,
                                            ChainTestUtil.blockHeader562463)

      val thirdBlockHeaderDb =
        BlockHeaderDbHelper.fromBlockHeader(firstPowChange,
                                            ChainTestUtil.blockHeader562464)

      /*
       * We need to insert one block before the first POW check because it is used on the next
       * POW check. We then need to insert the next to blocks to circumvent a POW check since
       * that would require we have an old block in the Blockchain that we don't have.
       */
      val firstThreeBlocks =
        Vector(firstBlockHeaderDb, secondBlockHeaderDb, thirdBlockHeaderDb)

      chainHandler.blockchain.blockHeaderDAO
        .createAll(firstThreeBlocks)
        .flatMap { _ =>
          var processorF = Future.successful(
            chainHandler.copy(blockchain =
              chainHandler.blockchain.copy(headers = firstThreeBlocks.reverse)))
          var blockCount = firstPowChange

          // Takes way too long to do all blocks
          val blockHeadersToTest = blockHeaders.tail.take(
            (2 * chainHandler.chainParams.difficultyChangeInterval + 1).toInt)

          val processFVec = blockHeadersToTest.map { blockHeader => () =>
            {
              processorF = processorF.flatMap { processor =>
                processor.processHeader(blockHeader)
              }

              processorF.flatMap { processor =>
                val headerFoundF = processor.getHeader(blockHeader.hashBE).map {
                  headerOpt =>
                    assert(headerOpt.map(_.blockHeader).contains(blockHeader))
                }

                val sizeChangedF = processor.getBlockCount.map { count =>
                  blockCount = blockCount + 1
                  assert(count == blockCount)
                }

                headerFoundF.flatMap(_ => sizeChangedF)
              }
            }
          }

          processFVec.foldLeft(Future.successful(succeed)) {
            case (fut, processF) =>
              fut.flatMap { _ =>
                processF()
              }
          }
        }
  }
}
