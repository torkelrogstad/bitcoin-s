package org.bitcoins.chain.pow

import akka.actor.ActorSystem
import org.bitcoins.chain.models.BlockHeaderDAO
import org.bitcoins.chain.util.ChainUnitTest
import org.bitcoins.core.protocol.blockchain.MainNetChainParams
import org.bitcoins.db.{AppConfig, UnitTestDbConfig}
import org.bitcoins.testkit.chain.ChainTestUtil
import org.scalatest.FutureOutcome

class BitcoinPowTest extends ChainUnitTest {

  override type FixtureParam = ChainFixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withChainFixture(test)

  override implicit val system: ActorSystem = ActorSystem("BitcoinPowTest")

  behavior of "BitcoinPow"

  it must "NOT calculate a POW change when one is not needed" inFixtured {
    case ChainFixture.Empty =>
      val chainParams = MainNetChainParams
      val appConfig = AppConfig(UnitTestDbConfig,chainParams)
      val blockHeaderDAO = BlockHeaderDAO(appConfig)
      val header1 = ChainTestUtil.ValidPOWChange.blockHeaderDb566494
      val header2 = ChainTestUtil.ValidPOWChange.blockHeaderDb566495

      val nextWorkF = Pow.getNetworkWorkRequired(header1,
                                                 header2.blockHeader,
                                                 blockHeaderDAO)

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

  it must "calculate a GetNextWorkRequired correctly" taggedAs FixtureTag.PopulatedBlockHeaderDAO inFixtured {
    case ChainFixture.PopulatedBlockHeaderDAO(blockHeaderDAO) =>
      val firstAfterAdjustmentF = blockHeaderDAO.getAtHeight(562464)
      val lastBeforeAdjustmentF = blockHeaderDAO.getAtHeight(564479)
      val nextAdjustmentF = blockHeaderDAO.getAtHeight(564480)

      for {
        firstBlockVec <- firstAfterAdjustmentF
        lastBlockVec <- lastBeforeAdjustmentF
        nextBlockVec <- nextAdjustmentF
      _ = {
        assert(firstBlockVec.length == 1)
        assert(lastBlockVec.length == 1)
        assert(nextBlockVec.length == 1)
      }
      nextNBits <- Pow.calculateNextWorkRequired(lastBlockVec.head, firstBlockVec.head, MainNetChainParams)
      } yield {
        assert(firstBlockVec.head.nBits == lastBlockVec.head.nBits)
        assert(lastBlockVec.head.nBits != nextBlockVec.head.nBits)
        assert(nextNBits == nextBlockVec.head.nBits)
      }
  }
}
