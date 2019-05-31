package org.bitcoins.core.p2p

import org.bitcoins.core.number.UInt64

/**
  * @see https://github.com/bitcoin/bitcoin/blob/fa2510d5c1cdf9c2cd5cc9887302ced4378c7202/src/protocol.h#L247
  */
sealed trait ServiceFlags {

  val value: UInt64
}

object ServiceFlags {
  final case object NODE_NONE extends ServiceFlags {
    val value: UInt64 = UInt64.zero
  }

  final object NODE_NETWORK extends ServiceFlags {
    val value: UInt64 = UInt64.one << 0
  }

  final object NODE_GETUTXO extends ServiceFlags {
    val value: UInt64 = UInt64.one << 1
  }

  final object NODE_BLOOM extends ServiceFlags {
    val value: UInt64 = UInt64.one << 2
  }

  final object NODE_WITNESS extends ServiceFlags {
    val value: UInt64 = UInt64.one << 3
  }

  final object NODE_XTHIN extends ServiceFlags {
    val value: UInt64 = UInt64.one << 4
  }

  final object NODE_NETWORK_LIMITED extends ServiceFlags {
    val value: UInt64 = UInt64.one << 10
  }

}
