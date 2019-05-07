package org.bitcoins.chain.config

import org.bitcoins.db._

case object ChainAppConfig extends AppConfig {
  override val moduleConfigName: String = "chain.conf"
}
