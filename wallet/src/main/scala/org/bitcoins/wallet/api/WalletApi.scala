package org.bitcoins.wallet.api

import org.bitcoins.core.config.NetworkParameters
import org.bitcoins.core.crypto._
import org.bitcoins.core.currency.CurrencyUnit
import org.bitcoins.core.hd.HDPurpose
import org.bitcoins.core.number.UInt32
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.core.protocol.blockchain.ChainParams
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.core.wallet.fee.FeeUnit
import org.bitcoins.wallet.config.WalletAppConfig
import org.bitcoins.wallet.db.WalletDbConfig
import org.bitcoins.wallet.HDUtil
import org.bitcoins.wallet.models.{AccountDb, AddressDb, UTXOSpendingInfoDb}

import scala.concurrent.Future

/**
  * API for the wallet project.
  *
  * This wallet API is BIP344 compliant.
  *
  * @see [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki BIP44]]
  */
sealed trait WalletApi {

  implicit val walletAppConfig: WalletAppConfig

  def dbConfig: WalletDbConfig = walletAppConfig.dbConfig

  def chainParams: ChainParams = walletAppConfig.chain

  def networkParameters: NetworkParameters = walletAppConfig.network
}

/**
  * API for a locked wallet
  */
trait LockedWalletApi extends WalletApi {

  /**
    * Adds the provided UTXO to the wallet, making it
    * available for spending.
    */
  def addUtxo(transaction: Transaction, vout: UInt32): Future[AddUtxoResult]

  /** Sums up the value of all UTXOs in the wallet */
  // noinspection AccessorLikeMethodIsEmptyParen
  // async calls have side effects :-)
  def getBalance(): Future[CurrencyUnit]

  /**
    * If a UTXO is spent outside of the wallet, we
    * need to remove it from the database so it won't be
    * attempted spent again by us.
    */
  // def updateUtxo: Future[WalletApi]

  def listUtxos(): Future[Vector[UTXOSpendingInfoDb[_]]]

  def listAddresses(): Future[Vector[AddressDb[_]]]

  protected[wallet] def defaultAccount: AccountDb

  /**
    * Gets a new external address. Calling this method multiple
    * times will return the same address, until it has
    * received funds.
    */
  def getNewAddress(account: AccountDb = defaultAccount): Future[BitcoinAddress]

  /**
    * Unlocks the wallet with the provided passphrase,
    * making it possible to send transactions.
    */
  def unlock(passphrase: AesPassword): Future[UnlockWalletResult]

  def listAccounts(): Future[Vector[AccountDb]]

  /**
    * Tries to create a new accoun in this wallet. Fails if the
    * most recent account has no transaction history, as per
    * BIP44
    *
    * @see [[https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki#account BIP44 account section]]
    */
  // def createNewAccount: Future[Try[WalletApi]]

}

trait UnlockedWalletApi extends LockedWalletApi {

  def mnemonicCode: MnemonicCode

  def passphrase: AesPassword

  /** Derives the relevant xpriv for the given HD purpose */
  def xprivForPurpose(purpose: HDPurpose): ExtPrivateKey = {
    val seed = BIP39Seed.fromMnemonic(mnemonicCode, BIP39Seed.EMPTY_PASSWORD) // todo think more about this

    val privVersion = HDUtil.getXprivVersion(purpose)
    //ExtKeyPrivVersion.fromNetworkParameters(networkParameters)
    seed.toExtPrivateKey(privVersion)
  }

  /**
    * Locks the wallet. After this operation is called,
    * all sensitive material in the wallet should be
    * encrypted and unaccessible
    */
  def lock: Future[LockedWalletApi]

  /**
    * todo: add error handling to signature
    */
  def sendToAddress(
      address: BitcoinAddress,
      amount: CurrencyUnit,
      feeRate: FeeUnit,
      fromAccount: AccountDb): Future[Transaction]
}
