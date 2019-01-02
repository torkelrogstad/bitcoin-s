package org.bitcoins.spvnode.serializers.messages.data

import org.bitcoins.core.protocol.transaction.Transaction
import org.bitcoins.core.serializers.RawBitcoinSerializer
import org.bitcoins.spvnode.messages.TransactionMessage
import org.bitcoins.spvnode.messages.data.TransactionMessage
import scodec.bits.ByteVector

/**
  * Created by chris on 6/2/16.
  * Responsible for serializing and deserializing TransactionMessage network objects
  * https://bitcoin.org/en/developer-reference#tx
  */
trait RawTransactionMessageSerializer extends RawBitcoinSerializer[TransactionMessage] {


  def read(bytes : ByteVector) : TransactionMessage = {
    val transaction = Transaction(bytes)
    TransactionMessage(transaction)
  }

  def write(transactionMessage: TransactionMessage) : ByteVector = {
    transactionMessage.transaction.bytes
  }
}

object RawTransactionMessageSerializer extends RawTransactionMessageSerializer