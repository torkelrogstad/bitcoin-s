package org.bitcoins.chain.validation

import org.bitcoins.chain.models.BlockHeaderDbHelper
import org.bitcoins.core.crypto.{DoubleSha256Digest, DoubleSha256DigestBE}
import org.bitcoins.core.protocol.blockchain.BlockHeader
import org.bitcoins.testkit.chain.{BlockHeaderHelper, ChainTestUtil}
import org.bitcoins.testkit.util.BitcoinSUnitTest
import org.scalatest.Assertion

class TipValidationTest extends BitcoinSUnitTest {

  behavior of "TipValidation"

  //blocks 566,092 and 566,093
  val newValidTip = BlockHeaderHelper.header1
  val currentTipDb = BlockHeaderHelper.header2Db
  val chainParam = ChainTestUtil.mainnetChainParam

  it must "connect two blocks with that are valid" in {

    val newValidTipDb = BlockHeaderDbHelper.fromBlockHeader(566093, newValidTip)
    val expected = TipUpdateResult.Success(newValidTipDb)

    runTest(newValidTip, expected)
  }

  it must "connect two blocks with different POW requirements at the correct interval (2016 blocks for BTC)"

  it must "fail to connect two blocks that do not reference prev block hash correctly" in {

    val badPrevHash = BlockHeaderHelper.badPrevHash

    val expected = TipUpdateResult.BadPreviousBlockHash(badPrevHash)

    runTest(badPrevHash, expected)
  }

  it must "fail to connect two blocks with two different POW requirements at the wrong interval" in {
    val badPOW = BlockHeaderHelper.badNBits
    val expected = TipUpdateResult.BadPOW(badPOW)
    runTest(badPOW, expected)
  }

  it must "fail to connect two blocks with a bad nonce" in {
    val badNonce = BlockHeaderHelper.badNonce
    val expected = TipUpdateResult.BadNonce(badNonce)
    runTest(badNonce, expected)
  }

  private def runTest(
      header: BlockHeader,
      expected: TipUpdateResult): Assertion = {
    val validationResult =
      TipValidation.checkNewTip(header, currentTipDb, chainParam)

    assert(validationResult == expected)
  }
}
