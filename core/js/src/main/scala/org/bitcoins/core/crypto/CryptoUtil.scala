package org.bitcoins.core.util

import java.security.MessageDigest

import org.bitcoins.core.crypto._
import scodec.bits.{BitVector, ByteVector}

/**
  * Utility cryptographic functions
  */
trait CryptoUtil extends BitcoinSLogger {

  /** Does the following computation: RIPEMD160(SHA256(hex)).*/
  def sha256Hash160(hex: String): Sha256Hash160Digest =
    sha256Hash160(BitcoinSUtil.decodeHex(hex))

  def sha256Hash160(bytes: ByteVector): Sha256Hash160Digest = {
    val hash = ripeMd160(sha256(bytes).bytes).bytes
    Sha256Hash160Digest(hash)
  }

  /** Performs sha256(sha256(hex)). */
  def doubleSHA256(hex: String): DoubleSha256Digest =
    doubleSHA256(BitcoinSUtil.decodeHex(hex))

  /** Performs sha256(sha256(bytes)). */
  def doubleSHA256(bytes: ByteVector): DoubleSha256Digest = {
    val hash: ByteVector = sha256(sha256(bytes).bytes).bytes
    DoubleSha256Digest(hash)
  }

  /** Takes sha256(hex). */
  def sha256(hex: String): Sha256Digest = sha256(BitcoinSUtil.decodeHex(hex))

  /** Takes sha256(bytes). */
  def sha256(bytes: ByteVector): Sha256Digest = {
    val hash = MessageDigest.getInstance("SHA-256").digest(bytes.toArray)
    Sha256Digest(ByteVector(hash))
  }

  /** Takes sha256(bits). */
  def sha256(bits: BitVector): Sha256Digest = {
    sha256(bits.toByteVector)
  }

  /** Performs SHA1(bytes). */
  def sha1(bytes: ByteVector): Sha1Digest = {
    val hash = MessageDigest.getInstance("SHA-1").digest(bytes.toArray).toList
    Sha1Digest(ByteVector(hash))
  }

  /** Performs SHA1(hex). */
  def sha1(hex: String): Sha1Digest = sha1(BitcoinSUtil.decodeHex(hex))

  /** Performs RIPEMD160(hex). */
  def ripeMd160(hex: String): RipeMd160Digest =
    ripeMd160(BitcoinSUtil.decodeHex(hex))

  /** Performs RIPEMD160(bytes). */
  def ripeMd160(bytes: ByteVector): RipeMd160Digest = ???

  val emptyDoubleSha256Hash = DoubleSha256Digest(
    "0000000000000000000000000000000000000000000000000000000000000000")

  /**
    * Calculates `HMAC-SHA512(key, data)`
    */
  def hmac512(key: ByteVector, data: ByteVector): ByteVector = ???

  /**
    * Recover public keys from a signature and the message that was signed. This method will return 2 public keys, and the signature
    * can be verified with both, but only one of them matches that private key that was used to generate the signature.
    *
    * @param signature       signature
    * @param message message that was signed
    * @return a (pub1, pub2) tuple where pub1 and pub2 are candidates public keys. If you have the recovery id  then use
    *         pub1 if the recovery id is even and pub2 if it is odd
    */
  def recoverPublicKey(
      signature: ECDigitalSignature,
      message: ByteVector): (ECPublicKey, ECPublicKey) = ???
}

object CryptoUtil extends CryptoUtil
