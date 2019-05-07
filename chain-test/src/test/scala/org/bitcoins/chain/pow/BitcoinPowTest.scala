package org.bitcoins.chain.pow

import akka.actor.ActorSystem
import org.bitcoins.chain.models.BlockHeaderDAO
import org.bitcoins.chain.util.{ChainFixture, ChainFixtureTag, ChainUnitTest}
import org.bitcoins.core.protocol.blockchain.MainNetChainParams
import org.bitcoins.testkit.chain.ChainTestUtil
import org.scalatest.FutureOutcome

import scala.concurrent.Future
import org.bitcoins.chain.config.ChainAppConfig

class BitcoinPowTest extends ChainUnitTest {

  override type FixtureParam = ChainFixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withChainFixture(test)

  override implicit val system: ActorSystem = ActorSystem("BitcoinPowTest")

  behavior of "BitcoinPow"

  it must "NOT calculate a POW change when one is not needed" inFixtured {
    case ChainFixture.Empty =>
      val appConfig = ChainAppConfig
      val blockHeaderDAO = BlockHeaderDAO(appConfig)
      val header1 = ChainTestUtil.ValidPOWChange.blockHeaderDb566494
      val header2 = ChainTestUtil.ValidPOWChange.blockHeaderDb566495

      val nextWorkF =
        Pow.getNetworkWorkRequired(header1, header2.blockHeader, blockHeaderDAO)

      nextWorkF.map(nextWork => assert(nextWork == header1.nBits))
  }

  it must "calculate a pow change as per the bitcoin network" inFixtured {
    case ChainFixture.Empty =>
      val firstBlockDb = ChainTestUtil.ValidPOWChange.blockHeaderDb564480
      val currentTipDb = ChainTestUtil.ValidPOWChange.blockHeaderDb566495
      val expectedNextWork =
        ChainTestUtil.ValidPOWChange.blockHeader566496.nBits
      val calculatedWorkF =
        Pow.calculateNextWorkRequired(currentTipDb,
                                      firstBlockDb,
                                      MainNetChainParams)

      calculatedWorkF.map(calculatedWork =>
        assert(calculatedWork == expectedNextWork))
  }

  it must "GetNextWorkRequired correctly" taggedAs ChainFixtureTag.PopulatedBlockHeaderDAO inFixtured {
    case ChainFixture.PopulatedBlockHeaderDAO(blockHeaderDAO) =>
      val iterations = 4200

      // We must start after the first POW change to avoid looking for a block we don't have
      val assertionFs =
        (FIRST_POW_CHANGE + 1 until FIRST_POW_CHANGE + 1 + iterations).map {
          height =>
            val blockF = blockHeaderDAO.getAtHeight(height).map(_.head)
            val nextBlockF = blockHeaderDAO.getAtHeight(height + 1).map(_.head)

            for {
              currentTip <- blockF
              nextTip <- nextBlockF
              nextNBits <- Pow.getNetworkWorkRequired(currentTip,
                                                      nextTip.blockHeader,
                                                      blockHeaderDAO)
            } yield assert(nextNBits == nextTip.nBits)
        }

      Future.sequence(assertionFs).map(_ => succeed)
  }
}
