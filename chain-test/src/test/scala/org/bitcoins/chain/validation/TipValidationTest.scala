package org.bitcoins.chain.validation

import org.bitcoins.chain.models.BlockHeaderDbHelper
import org.bitcoins.core.protocol.blockchain.BlockHeader
import org.bitcoins.testkit.chain.ChainTestUtil
import org.bitcoins.testkit.util.BitcoinSUnitTest

class TipValidationTest extends BitcoinSUnitTest {

  behavior of "TipValidation"

  it must "connect to blocks with that are valid" in {
    //blocks 566,092 and 566,093
    val newValidTip = ChainTestUtil.header1
    val currentTipDb = ChainTestUtil.header2Db

    val newValidTipDb = BlockHeaderDbHelper.fromBlockHeader(566093, newValidTip)

    val validationResult = TipValidation.checkNewTip(newValidTip, currentTipDb)

    assert(validationResult == TipUpdateResult.Success(newValidTipDb))
  }
}
