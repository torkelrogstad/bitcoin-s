package org.bitcoins.wallet
import org.bitcoins.core.hd.HDPurpose
import org.bitcoins.core.crypto.ExtKeyPrivVersion
import org.bitcoins.wallet.config.WalletAppConfig
import org.bitcoins.core.config.MainNet
import org.bitcoins.core.config.RegTest
import org.bitcoins.core.config.TestNet3
import org.bitcoins.core.crypto.ExtKeyVersion
import org.bitcoins.core.crypto.ExtKeyPubVersion
import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.hd.HDCoinType

private[wallet] object HDUtil {

  /** Gets the xpriv version required for the given HD purpose */
  def getXprivVersion(hdPurpose: HDPurpose)(
      implicit config: WalletAppConfig): ExtKeyPrivVersion = {
    import config.network
    import org.bitcoins.core.hd.HDPurposes._
    import ExtKeyVersion._

    (hdPurpose, network) match {
      case (SegWit, MainNet)                  => SegWitMainNetPriv
      case (SegWit, TestNet3 | RegTest)       => SegWitTestNet3Priv
      case (NestedSegWit, MainNet)            => NestedSegWitMainNetPriv
      case (NestedSegWit, TestNet3 | RegTest) => NestedSegWitTestNet3Priv
      case (Legacy, MainNet)                  => LegacyMainNetPriv
      case (Legacy, TestNet3 | RegTest)       => LegacyTestNet3Priv
      case (unknown: HDPurpose, _) =>
        throw new IllegalArgumentException(s"Got unknown HD purpose $unknown")
    }
  }

  /** Gets the xpub version required for the given HD purpose */
  def getXpubVersion(hdPurpose: HDPurpose)(
      implicit config: WalletAppConfig): ExtKeyPubVersion = {
    import config.network
    import org.bitcoins.core.hd.HDPurposes._
    import ExtKeyVersion._

    (hdPurpose, network) match {
      case (SegWit, MainNet)                  => SegWitMainNetPub
      case (SegWit, TestNet3 | RegTest)       => SegWitTestNet3Pub
      case (NestedSegWit, MainNet)            => NestedSegWitMainNetPub
      case (NestedSegWit, TestNet3 | RegTest) => NestedSegWitTestNet3Pub
      case (Legacy, MainNet)                  => LegacyMainNetPub
      case (Legacy, TestNet3 | RegTest)       => LegacyTestNet3Pub
      case (unknown: HDPurpose, _) =>
        throw new IllegalArgumentException(s"Got unknown HD purpose $unknown")
    }
  }

  def getCoinType(network: NetworkParameters): HDCoinType = network match {
    case MainNet            => HDCoinType.Bitcoin
    case TestNet3 | RegTest => HDCoinType.Testnet
  }
}
