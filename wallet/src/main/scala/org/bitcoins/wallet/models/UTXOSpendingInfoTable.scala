package org.bitcoins.wallet.models

import org.bitcoins.core.crypto.Sign
import org.bitcoins.core.currency.CurrencyUnit
import org.bitcoins.core.protocol.script.{ScriptPubKey, ScriptWitness}
import org.bitcoins.core.protocol.transaction.{
  TransactionOutPoint,
  TransactionOutput
}
import org.bitcoins.core.script.crypto.HashType
import org.bitcoins.core.wallet.utxo.BitcoinUTXOSpendingInfo
import org.bitcoins.db.{DbRowAutoInc, TableAutoInc}
import slick.jdbc.SQLiteProfile.api._
import slick.lifted.ProvenShape
import org.bitcoins.core.hd.HDPath
import scala.util.Failure
import scala.util.Success
import org.bitcoins.core.hd.SegWitHDPath

case class SegWitUTOXSpendingInfodb(
    id: Option[Long],
    outPoint: TransactionOutPoint,
    output: TransactionOutput,
    privKeyPath: SegWitHDPath,
    scriptWitness: ScriptWitness
) extends UTXOSpendingInfoDb[SegWitHDPath] {
  override def redeemScriptOpt: Option[ScriptPubKey] = None
  override def scriptWitnessOpt: Option[ScriptWitness] = Some(scriptWitness)

  override def copyWithId(id: Long): SegWitUTOXSpendingInfodb =
    copy(id = Some(id))
}

// TODO add case for nested segwit
// and legacy
sealed trait UTXOSpendingInfoDb[T <: HDPath[T]]
    extends DbRowAutoInc[UTXOSpendingInfoDb[T]] {
  def id: Option[Long]
  def outPoint: TransactionOutPoint
  def output: TransactionOutput
  def privKeyPath: T
  def redeemScriptOpt: Option[ScriptPubKey]
  def scriptWitnessOpt: Option[ScriptWitness]

  val hashType: HashType = HashType.sigHashAll

  def value: CurrencyUnit = output.value

  def toUTXOSpendingInfo(account: AccountDb): BitcoinUTXOSpendingInfo = {

    val pubAtPath = account.xpub.deriveChildPubKey(privKeyPath) match {
      case Failure(exception) => throw exception
      case Success(xpub)      => xpub.key
    }

    val sign: Sign = Sign(pubAtPath.signFunction, pubAtPath)

    BitcoinUTXOSpendingInfo(outPoint,
                            output,
                            List(sign),
                            redeemScriptOpt,
                            scriptWitnessOpt,
                            hashType)
  }

}

case class UTXOSpendingInfoTable(tag: Tag)
    extends TableAutoInc[UTXOSpendingInfoDb[_]](tag, "utxos") {
  import org.bitcoins.db.DbCommonsColumnMappers._

  def outPoint: Rep[TransactionOutPoint] =
    column[TransactionOutPoint]("tx_outpoint")

  def output: Rep[TransactionOutput] =
    column[TransactionOutput]("tx_output")

  def privKeyPath: Rep[HDPath[_]] = column[HDPath[_]]("hd_privkey_path")

  def redeemScriptOpt: Rep[Option[ScriptPubKey]] =
    column[Option[ScriptPubKey]]("nullable_redeem_script")

  def scriptWitnessOpt: Rep[Option[ScriptWitness]] =
    column[Option[ScriptWitness]]("script_witness")

  private type UTXOTuple[T <: HDPath[T]] = (
      Option[Long],
      TransactionOutPoint,
      TransactionOutput,
      HDPath[T],
      Option[ScriptPubKey],
      Option[ScriptWitness])

  private def fromTuple[T <: HDPath[T]](
      tuple: UTXOTuple[T]): UTXOSpendingInfoDb[T] = {
    tuple match {
      case (id,
            outpoint,
            output,
            path: SegWitHDPath,
            None,
            Some(scriptWitness)) =>
        SegWitUTOXSpendingInfodb(id, outpoint, output, path, scriptWitness)
          .asInstanceOf[UTXOSpendingInfoDb[T]]

      case bad =>
        throw new IllegalArgumentException(
          s"Could not construct UtxoSpendingInfoDb from bad combination $bad. Note: Only Segwit is implemented")
    }
  }

  private def toTuple[T <: HDPath[T]](
      utxo: UTXOSpendingInfoDb[T]): Option[UTXOTuple[T]] =
    Some(
      (utxo.id,
       utxo.outPoint,
       utxo.output,
       utxo.privKeyPath,
       utxo.redeemScriptOpt,
       utxo.scriptWitnessOpt))

  def * : ProvenShape[UTXOSpendingInfoDb[_]] = ???
}
