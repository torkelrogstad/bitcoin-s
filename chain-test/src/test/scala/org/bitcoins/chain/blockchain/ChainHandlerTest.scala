package org.bitcoins.chain.blockchain

import akka.actor.ActorSystem
import org.bitcoins.chain.util.ChainUnitTest
import org.bitcoins.rpc.util.RpcUtil
import org.bitcoins.testkit.chain.BlockHeaderHelper
import org.scalatest.FutureOutcome

import scala.concurrent.Future

class ChainHandlerTest extends ChainUnitTest {

  override type FixtureParam = ChainFixture

  override implicit val system = ActorSystem("ChainUnitTest")

  override val defaultTag: FixtureTag = FixtureTag.GenisisChainHandler

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withChainFixture(test)

  behavior of "ChainHandler"

  it must "process a new valid block header, and then be able to fetch that header" inFixtured {
    case ChainFixture.GenisisChainHandler(chainHandler) =>
      val newValidHeader = BlockHeaderHelper.buildNextHeader(genesisHeaderDb)
      val processedHeaderF =
        chainHandler.processHeader(newValidHeader.blockHeader)

      val foundHeaderF =
        processedHeaderF.flatMap(_.getHeader(newValidHeader.hashBE))

      foundHeaderF.map(found => assert(found.get == newValidHeader))
  }

  it must "be able to process and fetch real headers from mainnet" taggedAs FixtureTag.PopulatedChainHandler inFixtured {
    case ChainFixture.PopulatedChainHandler(chainHandler) =>
      val blockHeaderDAO = chainHandler.blockchain.blockHeaderDAO
      val blockHeadersF = blockHeaderDAO.getBetweenHeights(562375, 563375)

      var processorF = Future.successful(chainHandler)
      var blockCount = 1L

      val processVecF = blockHeadersF.flatMap { blockHeaders =>
        val processFVec = blockHeaders.map { blockHeader =>
          processorF = processorF.flatMap { processor =>
            processor.processHeader(blockHeader.blockHeader)
          }

          processorF.flatMap { processor =>
            val headerFoundF = processor.getHeader(blockHeader.hashBE).map { headerOpt =>
              assert(headerOpt.contains(blockHeader))
            }

            val sizeChangedF = processor.getBlockCount.map { count =>
              blockCount = blockCount + 1
              assert(count == blockCount)
            }

            headerFoundF.flatMap(_ => sizeChangedF)
          }
        }

        Future.sequence(processFVec)
      }

    processVecF.map(_ => succeed)
  }

}
