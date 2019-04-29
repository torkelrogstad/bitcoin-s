package org.bitcoins.core.hd

/**
  * Represents a
  * [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#change BIP44]]
  * change chain
  */
sealed abstract class HDChain extends BIP32Path {
  override val path: Vector[BIP32Node] = {
    account.path :+ BIP32Node(toInt, hardened = false)
  }

  def coin: HDCoin

  def account: HDAccount

  def chainType: HDChainType

  def toInt: Int = chainType.index

}

object HDChain {

  private case class BIP44ChainImpl(
      coin: HDCoin,
      chainType: HDChainType,
      account: HDAccount)
      extends HDChain

  def apply(chainType: HDChainType, account: HDAccount): HDChain =
    BIP44ChainImpl(account.coin, chainType, account)

}
