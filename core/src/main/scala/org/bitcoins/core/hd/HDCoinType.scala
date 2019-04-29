package org.bitcoins.core.hd

import org.bitcoins.core.protocol.blockchain.{
  ChainParams,
  MainNetChainParams,
  RegTestNetChainParams,
  TestNetChainParams
}

/**
  * Represents a
  * [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#Coin_type BIP44]],
  * [[https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki BIP84]]
  * and
  * [[https://github.com/bitcoin/bips/blob/master/bip-0049.mediawiki BIP49]]
  * coin type.
  */
sealed trait HDCoinType {
  def toInt: Int
}

/**
  * @see [[https://github.com/satoshilabs/slips/blob/master/slip-0044.md SLIP-0044]]
  *     central registry of coin types
  */
object HDCoinType {

  final case object Bitcoin extends HDCoinType {
    override val toInt: Int = 0
  }

  final case object Testnet extends HDCoinType {
    override val toInt: Int = 1
  }

  /**
    * Converts the given chain params into a HD coin type. Treats regtest and
    * testnet the same.
    *
    * @see [[https://github.com/bcoin-org/bcoin/blob/7c64fd845cbae23751558efbe8e078e2ccbfbd30/lib/protocol/networks.js#L838 bcoin]]
    *     and [[https://github.com/bitcoinj/bitcoinj/blob/bfe2a195b62bcbf1d2e678969e541ebc3656ae17/core/src/main/java/org/bitcoinj/params/RegTestParams.java#L48 BitcoinJ]]
    */
  def fromChainParams(chainParams: ChainParams): HDCoinType =
    chainParams match {
      case MainNetChainParams                         => Bitcoin
      case TestNetChainParams | RegTestNetChainParams => Testnet
    }

  def fromInt(int: Int): HDCoinType =
    int match {
      case Bitcoin.toInt => Bitcoin
      case Testnet.toInt => Testnet
      case _: Int =>
        throw new IllegalArgumentException(s"$int is not valid coin type!")
    }
}
