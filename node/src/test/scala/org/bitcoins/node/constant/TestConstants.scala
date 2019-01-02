package org.bitcoins.node.constant

import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

/**
  * Created by chris on 9/11/16.
  */
trait TestConstants extends DbConfig {

  /** Reads the configuration for the database specified inside of application.conf */
  def dbConfig: DatabaseConfig[PostgresProfile] =
    DatabaseConfig.forConfig("unitTestDatabaseUrl")

}

object TestConstants extends TestConstants
