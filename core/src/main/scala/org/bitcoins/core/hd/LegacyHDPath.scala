package org.bitcoins.core.hd

sealed abstract class LegacyHDPath extends HDPath[LegacyHDPath]

object LegacyHDPath extends HDPathFactory[LegacyHDPath] {

  /**
    * The purpose constant from BIP44
    *
    * @see [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#purpose BIP44]]
    */
  override val PURPOSE: Int = 44

  private case class LegacyHDPathImpl(address: HDAddress) extends LegacyHDPath

  override def apply(purpose: HDPurpose,
             coinType: HDCoinType,
             accountIndex: Int,
             chainType: HDChainType,
             addressIndex: Int): LegacyHDPath = {


val address = assembleAddress(purpose, coinType, accountIndex, chainType, addressIndex)
    LegacyHDPathImpl(address)
  }

}
