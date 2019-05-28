package org.bitcoins.core.crypto

object Secp256k1 {

  def pubKeyTweakAdd(
      pubkey: Array[Byte],
      tweak: Array[Byte],
      compressed: Boolean): Array[Byte] = ???

  def privKeyTweakAdd(pubkey: Array[Byte], tweak: Array[Byte]): Array[Byte] =
    ???

  def isValidPubKey(pubkey: Array[Byte]): Boolean = ???

  def sign(data: Array[Byte], seckey: Array[Byte]): Array[Byte] = ???

  def computePubkey(seckey: Array[Byte], compressed: Boolean): Array[Byte] = ???

  def secKeyVerify(seckey: Array[Byte]): Boolean = ???

  def verify(
      data: Array[Byte],
      signature: Array[Byte],
      pub: Array[Byte]): Boolean = ???

  def decompress(pubkey: Array[Byte]): Array[Byte] = ???
}
