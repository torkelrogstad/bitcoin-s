package org.bitcoins.chain.blockchain

import akka.actor.ActorSystem
import org.bitcoins.chain.models.BlockHeaderDAO
import org.bitcoins.testkit.chain.{BlockHeaderHelper, ChainUnitTest}
import org.scalatest.FutureOutcome

class BlockchainTest extends ChainUnitTest {

  override type FixtureParam = BlockHeaderDAO

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withBlockHeaderDAO(test)

  implicit override val system: ActorSystem = ActorSystem("BlockchainTest")

  behavior of "Blockchain"

  it must "connect a new header to the current tip of a blockchain" in {
    bhDAO: BlockHeaderDAO =>
      val blockchain = Blockchain.fromHeaders(
        headers = Vector(ChainUnitTest.genesisHeaderDb)
      )

      val newHeader =
        BlockHeaderHelper.buildNextHeader(ChainUnitTest.genesisHeaderDb)

      val connectTip =
        Blockchain.connectTip(header = newHeader.blockHeader, blockchain)

      connectTip match {
        case ConnectTipResult.ExtendChain(_, newChain) =>
          assert(newHeader == newChain.tip)

        case fail @ (_: ConnectTipResult.Reorg | _: ConnectTipResult.BadTip) =>
          assert(false)
      }
  }
}
