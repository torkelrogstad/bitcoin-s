package org.bitcoins.rpc.client

import org.bitcoins.core.crypto.{ DoubleSha256Digest, ECPrivateKey, ECPublicKey }
import org.bitcoins.core.currency.Bitcoins
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.core.protocol.blockchain.MerkleBlock
import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.rpc.client.RpcOpts.AddressType
import org.bitcoins.rpc.jsonmodels._
import org.bitcoins.rpc.serializers.BitcoindJsonWriters._
import org.bitcoins.rpc.serializers.BitcoindJsonSerializers._
import play.api.libs.json._

import scala.concurrent.Future

/**
 * RPC calls related to wallet management
 * functionality in bitcoind
 */
protected trait WalletRpc extends Client {

  def backupWallet(destination: String): Future[Unit] = {
    bitcoindCall[Unit]("backupwallet", List(JsString(destination)))
  }

  def dumpPrivKey(address: BitcoinAddress): Future[ECPrivateKey] = {
    bitcoindCall[String]("dumpprivkey", List(JsString(address.value)))
      .map(ECPrivateKey.fromWIFToPrivateKey)
  }

  def dumpWallet(filePath: String): Future[DumpWalletResult] = {
    bitcoindCall[DumpWalletResult]("dumpwallet", List(JsString(filePath)))
  }

  def encryptWallet(passphrase: String): Future[String] = {
    bitcoindCall[String]("encryptwallet", List(JsString(passphrase)))
  }

  def getAccount(address: BitcoinAddress): Future[String] = {
    bitcoindCall[String]("getaccount", List(JsString(address.value)))
  }

  def getAccountAddress(account: String): Future[BitcoinAddress] = {
    bitcoindCall[BitcoinAddress]("getaccountaddress", List(JsString(account)))
  }

  def getAddressesByAccount(account: String): Future[Vector[BitcoinAddress]] = {
    bitcoindCall[Vector[BitcoinAddress]](
      "getaddressesbyaccount",
      List(JsString(account)))
  }

  def getBalance: Future[Bitcoins] = {
    bitcoindCall[Bitcoins]("getbalance")
  }

  def getNewAddress: Future[BitcoinAddress] =
    getNewAddress(addressType = None)

  def getReceivedByAccount(
    account: String,
    confirmations: Int = 1): Future[Bitcoins] = {
    bitcoindCall[Bitcoins](
      "getreceivedbyaccount",
      List(JsString(account), JsNumber(confirmations)))
  }

  def getReceivedByAddress(
    address: BitcoinAddress,
    minConfirmations: Int = 1): Future[Bitcoins] = {
    bitcoindCall[Bitcoins](
      "getreceivedbyaddress",
      List(JsString(address.toString), JsNumber(minConfirmations)))
  }

  def getUnconfirmedBalance: Future[Bitcoins] = {
    bitcoindCall[Bitcoins]("getunconfirmedbalance")
  }

  def importAddress(
    address: BitcoinAddress,
    account: String = "",
    rescan: Boolean = true,
    p2sh: Boolean = false): Future[Unit] = {
    bitcoindCall[Unit](
      "importaddress",
      List(
        JsString(address.value),
        JsString(account),
        JsBoolean(rescan),
        JsBoolean(p2sh)))
  }

  def getNewAddress(account: String): Future[BitcoinAddress] =
    getNewAddress(account, None)

  private def getNewAddress(
    account: String = "",
    addressType: Option[AddressType]): Future[BitcoinAddress] = {
    val params =
      if (addressType.isEmpty) {
        List(JsString(account))
      } else {
        List(
          JsString(account),
          JsString(RpcOpts.addressTypeString(addressType.get)))
      }

    bitcoindCall[BitcoinAddress]("getnewaddress", params)
  }

  def getNewAddress(addressType: AddressType): Future[BitcoinAddress] =
    getNewAddress(addressType = Some(addressType))

  def getNewAddress(
    account: String,
    addressType: AddressType): Future[BitcoinAddress] =
    getNewAddress(account, Some(addressType))

  def getWalletInfo: Future[GetWalletInfoResult] = {
    bitcoindCall[GetWalletInfoResult]("getwalletinfo")
  }

  def keyPoolRefill(keyPoolSize: Int = 100): Future[Unit] = {
    bitcoindCall[Unit]("keypoolrefill", List(JsNumber(keyPoolSize)))
  }

  def importPubKey(
    pubKey: ECPublicKey,
    label: String = "",
    rescan: Boolean = true): Future[Unit] = {
    bitcoindCall[Unit](
      "importpubkey",
      List(JsString(pubKey.hex), JsString(label), JsBoolean(rescan)))
  }

  def importPrivKey(
    key: ECPrivateKey,
    account: String = "",
    rescan: Boolean = true): Future[Unit] = {
    bitcoindCall[Unit](
      "importprivkey",
      List(JsString(key.toWIF(network)), JsString(account), JsBoolean(rescan)))
  }

  def importMulti(
    requests: Vector[RpcOpts.ImportMultiRequest],
    rescan: Boolean = true): Future[Vector[ImportMultiResult]] = {
    bitcoindCall[Vector[ImportMultiResult]](
      "importmulti",
      List(Json.toJson(requests), JsObject(Map("rescan" -> JsBoolean(rescan)))))
  }

  def importPrunedFunds(
    transaction: Transaction,
    txOutProof: MerkleBlock): Future[Unit] = {
    bitcoindCall[Unit](
      "importprunedfunds",
      List(JsString(transaction.hex), JsString(txOutProof.hex)))
  }

  def removePrunedFunds(txid: DoubleSha256Digest): Future[Unit] = {
    bitcoindCall[Unit]("removeprunedfunds", List(JsString(txid.hex)))
  }

  def importWallet(filePath: String): Future[Unit] = {
    bitcoindCall[Unit]("importwallet", List(JsString(filePath)))
  }

  def listAccounts(
    confirmations: Int = 1,
    includeWatchOnly: Boolean = false): Future[Map[String, Bitcoins]] = {
    bitcoindCall[Map[String, Bitcoins]](
      "listaccounts",
      List(JsNumber(confirmations), JsBoolean(includeWatchOnly)))
  }

  def listAddressGroupings: Future[Vector[Vector[RpcAddress]]] = {
    bitcoindCall[Vector[Vector[RpcAddress]]]("listaddressgroupings")
  }

  def listReceivedByAccount(
    confirmations: Int = 1,
    includeEmpty: Boolean = false,
    includeWatchOnly: Boolean = false): Future[Vector[ReceivedAccount]] = {
    bitcoindCall[Vector[ReceivedAccount]](
      "listreceivedbyaccount",
      List(
        JsNumber(confirmations),
        JsBoolean(includeEmpty),
        JsBoolean(includeWatchOnly)))
  }

  def listReceivedByAddress(
    confirmations: Int = 1,
    includeEmpty: Boolean = false,
    includeWatchOnly: Boolean = false): Future[Vector[ReceivedAddress]] = {
    bitcoindCall[Vector[ReceivedAddress]](
      "listreceivedbyaddress",
      List(
        JsNumber(confirmations),
        JsBoolean(includeEmpty),
        JsBoolean(includeWatchOnly)))
  }

  def listWallets: Future[Vector[String]] = {
    bitcoindCall[Vector[String]]("listwallets")
  }

  def setAccount(address: BitcoinAddress, account: String): Future[Unit] = {
    bitcoindCall[Unit](
      "setaccount",
      List(JsString(address.value), JsString(account)))
  }

  // TODO: Should be BitcoinFeeUnit
  def setTxFee(feePerKB: Bitcoins): Future[Boolean] = {
    bitcoindCall[Boolean]("settxfee", List(JsNumber(feePerKB.toBigDecimal)))
  }

  def walletLock(): Future[Unit] = {
    bitcoindCall[Unit]("walletlock")
  }

  def walletPassphrase(passphrase: String, seconds: Int): Future[Unit] = {
    bitcoindCall[Unit](
      "walletpassphrase",
      List(JsString(passphrase), JsNumber(seconds)))
  }

  def walletPassphraseChange(
    currentPassphrase: String,
    newPassphrase: String): Future[Unit] = {
    bitcoindCall[Unit](
      "walletpassphrasechange",
      List(JsString(currentPassphrase), JsString(newPassphrase)))
  }
}
