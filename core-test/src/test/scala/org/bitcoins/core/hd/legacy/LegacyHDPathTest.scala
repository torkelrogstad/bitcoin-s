package org.bitcoins.core.hd.legacy

import org.bitcoins.core.hd.HDAccount
import org.bitcoins.testkit.core.gen.{
  CryptoGenerators,
  HDGenerators,
  NumberGenerator
}
import org.bitcoins.testkit.util.BitcoinSUnitTest

import scala.util.{Failure, Success, Try}
import org.bitcoins.core.hd.HDAddress
import org.bitcoins.core.hd.HDChainType
import org.bitcoins.core.hd.HDChain
import org.bitcoins.core.hd.HDCoin
import org.bitcoins.core.hd.HDCoinType
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bitcoins.core.hd.HDPath
import org.bitcoins.core.hd.HDPurpose
import org.bitcoins.core.hd.HDPurposes
import org.bitcoins.core.hd.LegacyHDPath
import org.bitcoins.core.hd.SegWitHDPath
import org.bitcoins.core.hd.NestedSegWitHDPath

class LegacyHDPathTest extends BitcoinSUnitTest {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    generatorDrivenConfigNewCode

  behavior of "HDAccount"

  it must "fail to make accounts with negative indices" in {
    forAll(HDGenerators.hdCoin, NumberGenerator.negativeInts) { (coin, i) =>
      assertThrows[IllegalArgumentException](HDAccount(coin = coin, index = i))
    }
  }

  it must "be convertable to a HDChain" in {
    forAll(HDGenerators.hdAccount, HDGenerators.hdChainType) {
      (account, chainType) =>
        val chain = account.toChain(chainType)
        assert(chain.account == account)
    }
  }

  behavior of "HDAddress"

  it must "fail to make addresses with neagtives indices" in {
    forAll(HDGenerators.hdChain, NumberGenerator.negativeInts) { (chain, i) =>
      assertThrows[IllegalArgumentException](
        HDAddress(chain = chain, index = i))
    }
  }

  it must "be convertable to a HD path" in {
    forAll(HDGenerators.hdAddress, HDGenerators.hdPurpose) { (addr, purpose) =>
      val path = addr.toPath(purpose)
      assert(path.address == addr)

    }
  }

  behavior of "HDChainType"

  it must "correctly represent external and change chains" in {
    HDChainType.fromInt(0) must be(HDChainType.External)
    HDChainType.fromInt(1) must be(HDChainType.Change)

    forAll(NumberGenerator.ints.suchThat(i => i != 1 && i != 0)) { i =>
      assertThrows[IllegalArgumentException](HDChainType.fromInt(i))
    }
  }

  behavior of "HDCoinType"

  it must "correctly represent Bitcoin and Testnet coins" in {
    HDCoinType.fromInt(0) must be(HDCoinType.Bitcoin)
    HDCoinType.fromInt(1) must be(HDCoinType.Testnet)
    forAll(NumberGenerator.ints.suchThat(i => i != 1 && i != 0)) { i =>
      assertThrows[IllegalArgumentException](HDCoinType.fromInt(i))
    }
  }

  it must "be convertable to a HDAccount" in {
    forAll(HDGenerators.hdCoin, NumberGenerator.positiveInts) { (coin, index) =>
      val account = coin.toAccount(index)
      assert(account.coin == coin)
    }
  }

  behavior of "HDPath"

  it must "have toString/fromString symmetry" in {
    forAll(HDGenerators.hdPath) { path =>
      val pathFromString = HDPath.fromString(path.toString)
      pathFromString match {
        case Some(value: LegacyHDPath) =>
          assert(value == path.asInstanceOf[LegacyHDPath])
        case Some(value: SegWitHDPath) =>
          assert(value == path.asInstanceOf[SegWitHDPath])
        case Some(value: NestedSegWitHDPath) =>
          assert(value == path.asInstanceOf[NestedSegWitHDPath])
        case Some(other) => fail(s"$other is unknown HD path type!")
        case None        => fail(s"$path did not have toString/fromString symmetry")
      }
    }
  }

  it must "fail to generate a HD path with an invalid purpose field" in {
    val badPaths = HDGenerators.bip32Path.suchThat { bip32 =>
      bip32.path.nonEmpty &&
      HDPurposes.all.find(_.constant == bip32.path.head.index).isEmpty
    }

    forAll(badPaths) { badPath =>
      val attempt = HDPath.fromString(badPath.toString)
      attempt match {
        case None =>
          succeed
        case Some(_) => fail
      }
    }
  }

  it must "fail to generate HD paths with an invalid length" in {
    forAll(HDGenerators.hdPathWithConstructor) {
      case (hd, hdApply) =>
        val tooShortPath = hd.path.dropRight(1)
        val attempt = hdApply(tooShortPath)
        attempt match {
          case Success(_) => fail
          case Failure(exception) =>
            assert(exception.getMessage.contains("must have five elements"))
        }
    }
  }

  it must "fail to generate HD paths with the wrong hardened index types" in {
    forAll(HDGenerators.hdPathWithConstructor) {
      case (hd, hdApply) =>
        val nonHardenedCoinChildren = hd.path.zipWithIndex.map {
          case (child, index) =>
            if (index == LegacyHDPath.COIN_INDEX) child.copy(hardened = false)
            else child
        }

        val badCoinAttempt = hdApply(nonHardenedCoinChildren)

        badCoinAttempt match {
          case Success(_) => fail
          case Failure(exc) =>
            assert(exc.getMessage.contains("coin type child must be hardened"))
        }

        val nonHardenedAccountChildren = hd.path.zipWithIndex.map {
          case (child, index) =>
            if (index == LegacyHDPath.ACCOUNT_INDEX)
              child.copy(hardened = false)
            else child
        }
        val badAccountAttempt = hdApply(nonHardenedAccountChildren)

        badAccountAttempt match {
          case Success(_) => fail
          case Failure(exc) =>
            assert(exc.getMessage.contains("account child must be hardened"))
        }

        val hardenedChainChildren = hd.path.zipWithIndex.map {
          case (child, index) =>
            if (index == LegacyHDPath.CHAIN_INDEX) child.copy(hardened = true)
            else child
        }
        val badChainAttempt =
          hdApply(hardenedChainChildren)

        badChainAttempt match {
          case Success(_) => fail
          case Failure(exc) =>
            assert(exc.getMessage.contains("chain child must not be hardened"))
        }

        val hardenedAddressChildren = hd.path.zipWithIndex.map {
          case (child, index) =>
            if (index == LegacyHDPath.ADDRESS_INDEX) child.copy(hardened = true)
            else child
        }
        val badAddrAttempt =
          hdApply(hardenedAddressChildren)

        badAddrAttempt match {
          case Success(_) => fail
          case Failure(exc) =>
            assert(
              exc.getMessage.contains(
                "address index child must not be hardened"))
        }
    }
  }

  // https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#examples
  it must "correctly parse the examples from BIP44" in {

    val firstString = " m / 44' / 0' / 0' / 0 / 0 "
    val first = LegacyHDPath.fromString(firstString)
    assert(first.purpose == HDPurposes.Legacy)
    assert(first.coin.coinType == HDCoinType.Bitcoin)
    assert(first.account.index == 0)
    assert(first.chain.chainType == HDChainType.External)
    assert(first.address.index == 0)
    assert(HDPath.fromString(firstString).contains(first))

    val secondString = " m / 44' / 0' / 0' / 0 / 1 "
    val second = LegacyHDPath.fromString(secondString)
    assert(second.purpose == HDPurposes.Legacy)
    assert(second.coin.coinType == HDCoinType.Bitcoin)
    assert(second.account.index == 0)
    assert(second.chain.chainType == HDChainType.External)
    assert(second.address.index == 1)
    assert(HDPath.fromString(secondString).contains(second))

    val thirdString = " m / 44' / 0' / 0' / 1 / 0 "
    val third = LegacyHDPath.fromString(thirdString)
    assert(third.purpose == HDPurposes.Legacy)
    assert(third.coin.coinType == HDCoinType.Bitcoin)
    assert(third.account.index == 0)
    assert(third.chain.chainType == HDChainType.Change)
    assert(third.address.index == 0)
    assert(HDPath.fromString(thirdString).contains(third))

    val fourthString = " m / 44' / 0' / 0' / 1 / 1 "
    val fourth = LegacyHDPath.fromString(fourthString)
    assert(fourth.purpose == HDPurposes.Legacy)
    assert(fourth.coin.coinType == HDCoinType.Bitcoin)
    assert(fourth.account.index == 0)
    assert(fourth.chain.chainType == HDChainType.Change)
    assert(fourth.address.index == 1)
    assert(HDPath.fromString(fourthString).contains(fourth))

    val fifthString = " m / 44' / 0' / 1' / 0 / 0 "
    val fifth = LegacyHDPath.fromString(fifthString)
    assert(fifth.purpose == HDPurposes.Legacy)
    assert(fifth.coin.coinType == HDCoinType.Bitcoin)
    assert(fifth.account.index == 1)
    assert(fifth.chain.chainType == HDChainType.External)
    assert(fifth.address.index == 0)
    assert(HDPath.fromString(fifthString).contains(fifth))

    val sixthString = " m / 44' / 0' / 1' / 0 / 1 "
    val sixth = LegacyHDPath.fromString(sixthString)
    assert(sixth.purpose == HDPurposes.Legacy)
    assert(sixth.coin.coinType == HDCoinType.Bitcoin)
    assert(sixth.account.index == 1)
    assert(sixth.chain.chainType == HDChainType.External)
    assert(sixth.address.index == 1)
    assert(HDPath.fromString(sixthString).contains(sixth))

    val seventhString = " m / 44' / 0' / 1' / 1 / 0 "
    val seventh = LegacyHDPath.fromString(seventhString)
    assert(seventh.purpose == HDPurposes.Legacy)
    assert(seventh.coin.coinType == HDCoinType.Bitcoin)
    assert(seventh.account.index == 1)
    assert(seventh.chain.chainType == HDChainType.Change)
    assert(seventh.address.index == 0)
    assert(HDPath.fromString(seventhString).contains(seventh))

    val eightString = " m / 44' / 0' / 1' / 1 / 1 "
    val eigth = LegacyHDPath.fromString(eightString)
    assert(eigth.purpose == HDPurposes.Legacy)
    assert(eigth.coin.coinType == HDCoinType.Bitcoin)
    assert(eigth.account.index == 1)
    assert(eigth.chain.chainType == HDChainType.Change)
    assert(eigth.address.index == 1)
    assert(HDPath.fromString(eightString).contains(eigth))

    val ninthString = " m / 44' / 1' / 0' / 0 / 1 "
    val ninth = LegacyHDPath.fromString(ninthString)
    assert(ninth.purpose == HDPurposes.Legacy)
    assert(ninth.coin.coinType == HDCoinType.Testnet)
    assert(ninth.account.index == 0)
    assert(ninth.chain.chainType == HDChainType.External)
    assert(ninth.address.index == 1)
    assert(HDPath.fromString(ninthString).contains(ninth))

    val tenthString = " m / 44' / 1' / 0' / 0 / 1 "
    val tenth = LegacyHDPath.fromString(tenthString)
    assert(tenth.purpose == HDPurposes.Legacy)
    assert(tenth.coin.coinType == HDCoinType.Testnet)
    assert(tenth.account.index == 0)
    assert(tenth.chain.chainType == HDChainType.External)
    assert(tenth.address.index == 1)
    assert(HDPath.fromString(tenthString).contains(tenth))

    val eleventhString = " m / 44' / 1' / 0' / 1 / 0 "
    val eleventh = LegacyHDPath.fromString(eleventhString)
    assert(eleventh.purpose == HDPurposes.Legacy)
    assert(eleventh.coin.coinType == HDCoinType.Testnet)
    assert(eleventh.account.index == 0)
    assert(eleventh.chain.chainType == HDChainType.Change)
    assert(eleventh.address.index == 0)
    assert(HDPath.fromString(eleventhString).contains(eleventh))

    val twelfthString = " m / 44' / 1' / 0' / 1 / 1 "
    val twelfth = LegacyHDPath.fromString(twelfthString)
    assert(twelfth.purpose == HDPurposes.Legacy)
    assert(twelfth.coin.coinType == HDCoinType.Testnet)
    assert(twelfth.account.index == 0)
    assert(twelfth.chain.chainType == HDChainType.Change)
    assert(twelfth.address.index == 1)
    assert(HDPath.fromString(twelfthString).contains(twelfth))

    val thirteenthString = " m / 44' / 1' / 1' / 0 / 0 "
    val thirteenth = LegacyHDPath.fromString(thirteenthString)
    assert(thirteenth.purpose == HDPurposes.Legacy)
    assert(thirteenth.coin.coinType == HDCoinType.Testnet)
    assert(thirteenth.account.index == 1)
    assert(thirteenth.chain.chainType == HDChainType.External)
    assert(thirteenth.address.index == 0)
    assert(HDPath.fromString(thirteenthString).contains(thirteenth))

    val fourteenthString = " m / 44' / 1' / 1' / 0 / 1 "
    val fourteenth = LegacyHDPath.fromString(fourteenthString)
    assert(fourteenth.purpose == HDPurposes.Legacy)
    assert(fourteenth.coin.coinType == HDCoinType.Testnet)
    assert(fourteenth.account.index == 1)
    assert(fourteenth.chain.chainType == HDChainType.External)
    assert(fourteenth.address.index == 1)
    assert(HDPath.fromString(fourteenthString).contains(fourteenth))

    val fifteenthString = " m / 44' / 1' / 1' / 1 / 0 "
    val fifteenth = LegacyHDPath.fromString(fifteenthString)
    assert(fifteenth.purpose == HDPurposes.Legacy)
    assert(fifteenth.coin.coinType == HDCoinType.Testnet)
    assert(fifteenth.account.index == 1)
    assert(fifteenth.chain.chainType == HDChainType.Change)
    assert(fifteenth.address.index == 0)
    assert(HDPath.fromString(fifteenthString).contains(fifteenth))

    val sixteenthString = " m / 44' / 1' / 1' / 1 / 1 "
    val sixteenth = LegacyHDPath.fromString(sixteenthString)
    assert(sixteenth.purpose == HDPurposes.Legacy)
    assert(sixteenth.coin.coinType == HDCoinType.Testnet)
    assert(sixteenth.account.index == 1)
    assert(sixteenth.chain.chainType == HDChainType.Change)
    assert(sixteenth.address.index == 1)
    assert(HDPath.fromString(sixteenthString).contains(sixteenth))

  }

  it must "correctly parse the example from BIP84" ignore ???

  it must "correctly parse the example from BIP49" ignore ???

}
