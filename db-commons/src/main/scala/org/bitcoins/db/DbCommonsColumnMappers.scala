package org.bitcoins.db

import org.bitcoins.core.crypto._
import org.bitcoins.core.number.{Int32, UInt32, UInt64}
import org.bitcoins.core.protocol.BitcoinAddress
import org.bitcoins.core.protocol.script.{ScriptPubKey, ScriptWitness}
import org.bitcoins.core.protocol.transaction.{
  TransactionOutPoint,
  TransactionOutput
}
import org.bitcoins.core.script.ScriptType
import org.bitcoins.core.serializers.script.RawScriptWitnessParser
import scodec.bits.ByteVector
import slick.jdbc.SQLiteProfile.api._
import org.bitcoins.core.hd.LegacyHDPath
import org.bitcoins.core.hd.HDCoinType
import org.bitcoins.core.hd.HDChainType

abstract class DbCommonsColumnMappers {

  /** Responsible for mapping a [[DoubleSha256Digest]] to a String, and vice versa */
  implicit val doubleSha256DigestMapper: BaseColumnType[DoubleSha256Digest] =
    MappedColumnType.base[DoubleSha256Digest, String](
      _.hex,
      DoubleSha256Digest.fromHex
    )

  implicit val doubleSha256DigestBEMapper: BaseColumnType[
    DoubleSha256DigestBE] =
    MappedColumnType.base[DoubleSha256DigestBE, String](
      _.hex,
      DoubleSha256DigestBE.fromHex
    )

  implicit val ecPublicKeyMapper: BaseColumnType[ECPublicKey] =
    MappedColumnType.base[ECPublicKey, String](_.hex, ECPublicKey.fromHex)

  implicit val sha256Hash160DigestMapper: BaseColumnType[Sha256Hash160Digest] =
    MappedColumnType
      .base[Sha256Hash160Digest, String](_.hex, Sha256Hash160Digest.fromHex)

  /** Responsible for mapping a [[UInt32]] to a long in Slick, and vice versa */
  implicit val uInt32Mapper: BaseColumnType[UInt32] =
    MappedColumnType.base[UInt32, Long](
      tmap = _.toLong,
      tcomap = UInt32(_)
    )

  implicit val int32Mapper: BaseColumnType[Int32] = {
    MappedColumnType.base[Int32, Long](tmap = _.toLong, tcomap = Int32(_))
  }

  /** Responsible for mapping a [[TransactionOutput]] to hex in Slick, and vice versa */
  implicit val transactionOutputMapper: BaseColumnType[TransactionOutput] = {
    MappedColumnType.base[TransactionOutput, String](
      _.hex,
      TransactionOutput(_)
    )
  }

  implicit val uint64Mapper: BaseColumnType[UInt64] = {
    MappedColumnType.base[UInt64, BigDecimal](
      { u64: UInt64 =>
        BigDecimal(u64.toBigInt.bigInteger)
      },
      //this has the potential to throw
      { bigDec: BigDecimal =>
        UInt64(bigDec.toBigIntExact().get)
      }
    )
  }

  implicit val transactionOutPointMapper: BaseColumnType[TransactionOutPoint] = {
    MappedColumnType
      .base[TransactionOutPoint, String](_.hex, TransactionOutPoint(_))
  }

  implicit val scriptPubKeyMapper: BaseColumnType[ScriptPubKey] = {
    MappedColumnType.base[ScriptPubKey, String](_.hex, ScriptPubKey(_))
  }

  implicit val scriptWitnessMapper: BaseColumnType[ScriptWitness] = {
    MappedColumnType
      .base[ScriptWitness, String](
        _.hex,
        hex => RawScriptWitnessParser.read(ByteVector.fromValidHex(hex)))
  }

  implicit val byteVectorMapper: BaseColumnType[ByteVector] = {
    MappedColumnType
      .base[ByteVector, String](_.toHex, ByteVector.fromValidHex(_))
  }

  implicit val xpubMapper: BaseColumnType[ExtPublicKey] = {
    MappedColumnType
      .base[ExtPublicKey, String](_.toString, ExtPublicKey.fromString(_).get)
  }

  implicit val bip44CoinTypeMapper: BaseColumnType[HDCoinType] = {
    MappedColumnType.base[HDCoinType, Int](_.toInt, HDCoinType.fromInt)
  }

  implicit val bip44PathMappper: BaseColumnType[LegacyHDPath] =
    MappedColumnType
      .base[LegacyHDPath, String](_.toString, LegacyHDPath.fromString)

  implicit val bip44ChainTypeMapper: BaseColumnType[HDChainType] =
    MappedColumnType.base[HDChainType, Int](_.index, HDChainType.fromInt)

  implicit val bitcoinAddressMapper: BaseColumnType[BitcoinAddress] =
    MappedColumnType
      .base[BitcoinAddress, String](_.value, BitcoinAddress.fromStringExn)

  implicit val scriptTypeMapper: BaseColumnType[ScriptType] =
    MappedColumnType
      .base[ScriptType, String](_.toString, ScriptType.fromStringExn)

  implicit val aesSaltMapper: BaseColumnType[AesSalt] =
    MappedColumnType.base[AesSalt, String](
      _.value.toHex,
      hex => AesSalt(ByteVector.fromValidHex(hex)))

}

object DbCommonsColumnMappers extends DbCommonsColumnMappers
