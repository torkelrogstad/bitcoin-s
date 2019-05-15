package org.bitcoins.testkit.chain.fixture

import org.bitcoins.testkit.chain.ChainUnitTest
import org.bitcoins.testkit.chain.fixture.ChainFixture.{
  BitcoindZmqChainHandlerWithBlock,
  Empty,
  GenisisBlockHeaderDAO,
  GenisisChainHandler,
  PopulatedBlockHeaderDAO,
  PopulatedChainHandler
}

import scala.concurrent.Future

trait ChainFixtureHelper { this: ChainUnitTest =>

  def createFixture(tag: ChainFixtureTag): Future[ChainFixture] = {
    tag match {
      case ChainFixtureTag.Empty => Future.successful(ChainFixture.Empty)
      case ChainFixtureTag.GenisisBlockHeaderDAO =>
        createBlockHeaderDAO().map(GenisisBlockHeaderDAO.apply)
      case ChainFixtureTag.PopulatedBlockHeaderDAO =>
        createPopulatedBlockHeaderDAO().map(PopulatedBlockHeaderDAO.apply)
      case ChainFixtureTag.GenisisChainHandler =>
        createChainHandler().map(GenisisChainHandler.apply)
      case ChainFixtureTag.PopulatedChainHandler =>
        createPopulatedChainHandler().map(
          ChainFixture.PopulatedChainHandler.apply)
      case ChainFixtureTag.BitcoindZmqChainHandlerWithBlock =>
        createBitcoindChainHandlerViaZmq().map(
          BitcoindZmqChainHandlerWithBlock.apply)
    }
  }

  def destroyFixture(fixture: ChainFixture): Future[Any] = {
    fixture match {
      case Empty                      => Future.successful(())
      case GenisisBlockHeaderDAO(_)   => destroyHeaderTable()
      case PopulatedBlockHeaderDAO(_) => destroyHeaderTable()
      case GenisisChainHandler(_)     => destroyHeaderTable()
      case PopulatedChainHandler(_)   => destroyHeaderTable()
      case BitcoindZmqChainHandlerWithBlock(bitcoindHandler) =>
        destroyBitcoindChainHandlerViaZmq(bitcoindHandler)
    }
  }
}