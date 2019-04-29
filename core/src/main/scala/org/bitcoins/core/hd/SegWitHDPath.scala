package org.bitcoins.core.hd

sealed abstract class SegWitHDPath extends HDPath [SegWitHDPath]

object SegWitHDPath extends HDPathFactory[SegWitHDPath] {

 /**
   * The purpose constant from BIP84
   *
   * @see [[https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki BIP84]]
   */
 override val PURPOSE = 84

 private case class SegWitHDPathImpl(address: HDAddress) extends SegWitHDPath


 override def apply(purpose: HDPurpose, coinType: HDCoinType, accountIndex: Int, chainType: HDChainType, addressIndex: Int): SegWitHDPath = {
  val address = assembleAddress(purpose, coinType, accountIndex, chainType, addressIndex)
  SegWitHDPathImpl(address)

 }
}
