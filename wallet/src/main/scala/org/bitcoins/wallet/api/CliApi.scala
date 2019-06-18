package org.bitcoins.wallet.api

import scala.concurrent.Future
import org.bitcoins.core.currency.Bitcoins

trait CliApi {
  def balance: Future[Bitcoins]
}
