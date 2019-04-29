package org.bitcoins.core.hd


sealed abstract class NestedSegWitHDPath extends HDPath[NestedSegWitHDPath]

object NestedSegWitHDPath extends HDPathFactory[NestedSegWitHDPath] {

  /**
    * The purpose constant from BIP49
    *
    * @see [[https://github.com/bitcoin/bips/blob/master/bip-0049.mediawiki BIP49]]
    */
  override val PURPOSE: Int = 49

  private case class NestedSegWitHDPathImpl(address: HDAddress) extends NestedSegWitHDPath

  override def apply(purpose: HDPurpose, coinType: HDCoinType, accountIndex: Int, chainType: HDChainType, addressIndex: Int): NestedSegWitHDPath = {

    val address = assembleAddress(purpose, coinType, accountIndex, chainType, addressIndex)
    NestedSegWitHDPathImpl(address)
  }
}
