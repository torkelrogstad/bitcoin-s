package org.bitcoins.chain.validation

import org.bitcoins.chain.models.{BlockHeaderDb, BlockHeaderDbHelper}
import org.bitcoins.core.protocol.blockchain.{BlockHeader, ChainParams}
import org.bitcoins.core.util.BitcoinSLogger

sealed abstract class TipValidation extends BitcoinSLogger {

  /** Checks if the given header can be connected to the current tip
    * This is the method where a [[BlockHeader]] is transformed into a
    * [[BlockHeaderDb]]. What this really means is that a height is
    * assigned to a [[BlockHeader header]] after all these
    * validation checks occur
    * */
  def checkNewTip(
      newPotentialTip: BlockHeader,
      currentTip: BlockHeaderDb,
      chainParams: ChainParams): TipUpdateResult = {
    val header = newPotentialTip
    logger.debug(
      s"Checking header=${header.hashBE.hex} to try to connect to currentTip=${currentTip.hashBE.hex} with height=${currentTip.height}")

    val connectTipResult = {
      if (header.previousBlockHashBE != currentTip.hashBE) {
        logger.warn(
          s"Failed to connect tip=${header.hashBE.hex} to current chain")
        TipUpdateResult.BadPreviousBlockHash(newPotentialTip)
      } else if (header.nBits != currentTip.nBits) {
        //TODO: THis is a bug, this should only occurr only 2016 blocks
        //also this doesn't apply on testnet/regtest
        TipUpdateResult.BadPOW(newPotentialTip)
      } else if (isBadNonce(newPotentialTip)) {
        TipUpdateResult.BadNonce(newPotentialTip)
      } else {
        val headerDb = BlockHeaderDbHelper.fromBlockHeader(
          height = currentTip.height + 1,
          bh = newPotentialTip
        )
        TipUpdateResult.Success(headerDb)
      }
    }

    logTipResult(connectTipResult, currentTip)

    connectTipResult
  }

  /** Logs the result of [[org.bitcoins.chain.validation.TipValidation.checkNewTip() checkNewTip]] */
  private def logTipResult(
      connectTipResult: TipUpdateResult,
      currentTip: BlockHeaderDb): Unit = {
    connectTipResult match {
      case TipUpdateResult.Success(tipDb) =>
        logger.info(
          s"Successfully connected ${tipDb.hashBE.hex} with height=${tipDb.height} to block=${currentTip.hashBE.hex} with height=${currentTip.height}")

      case bad: TipUpdateResult.Failure =>
        logger.warn(
          s"Failed to connect ${bad.header.hashBE.hex} to ${currentTip.hashBE.hex} with height=${currentTip.height}, reason=${bad}")
    }
  }

  /** Checks if [[header.nonce]] hashes to meet the POW requirements for this block (nBits) */
  private def isBadNonce(header: BlockHeader): Boolean = {
    //TODO: needs to be implemented
    false
  }
}

object TipValidation extends TipValidation
