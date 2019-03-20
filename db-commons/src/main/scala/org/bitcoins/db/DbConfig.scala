package org.bitcoins.db

import java.io.File

import com.typesafe.config.Config
import org.bitcoins.core.util.BitcoinSLogger
import slick.basic.DatabaseConfig
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

/**
  * This is meant to encapsulate all of our database configuration.
  *
  * For each sub-project that needs a DB (currently wallet, node
  * and chain), there are 4 "network" databases. The following
  * directories are created under `$HOME/.bitcoin-s`
  *
  * <ol>
  *   <li>
  *     `mainnet` - this stores things related to the
  *     [[org.bitcoins.core.protocol.blockchain.MainNetChainParams MainNet]] network
  *   </li>
  *   <li>
  *     `testnet3` - this stores things related to the
  *     [[org.bitcoins.core.config.TestNet3 testnet3]] network
  *   </li>
  *   <li>
  *     `regtest` - this stores things related your local
  *     [[org.bitcoins.core.config.RegTest regtest]] network
  *   </li>
  *   <li>
  *     `unittest` - this stores things related to unit tests. Unit tests are free to
  *     create and destroy databases at will in this directory, so you should not
  *     store anything there.
  *   </li>
  * </ol>
  *
  * In order to create a database configuraion for a new project,
  * you must create a file `module.conf` in the resource directory
  * of your project. This file must define the following configuration
  * settings:
  *
  * {{{
  *   specificDbSettings.dbName
  *   specificDbSettings.user
  * }}}
  *
  * An example of a project creating their own database configuration can be seen
  * in the node project
  * [[com.sun.xml.internal.bind.v2.TODO here]].
  */
sealed abstract class DbConfig extends BitcoinSLogger {

  /** This is the key we look for in the config file
    * to identify a database database. An example
    * of this for the [[MainNetDbConfig]] is ''mainnetDb''
    */
  def configKey: String

  /** The configuration details for connecting/using the database for our projects
    * that require datbase connections
    * */
  lazy val dbConfig: DatabaseConfig[SQLiteProfile] = {
    //if we don't pass specific class, non-deterministic
    //errors around the loaded configuration depending
    //on the state of the default classLoader
    //https://github.com/lightbend/config#debugging-your-configuration
    val dbConfig: DatabaseConfig[SQLiteProfile] = {
      DatabaseConfig.forConfig(path = configKey,
                               classLoader = getClass.getClassLoader)
    }

    logger.trace(s"Resolved DB config: ${dbConfig.config}")

    createDbFileIfDNE(config = dbConfig.config)

    dbConfig
  }

  /** The database we are connecting to for our spv node */
  def database: Database = {
    dbConfig.db
  }

  private def createDbFileIfDNE(config: Config): Boolean = {
    val resolvedConfig = config.resolve()
    //should add a check in here that we are using sqlite
    val dbPath = new File(resolvedConfig.getString("dbPath"))
    if (!dbPath.exists()) {
      logger.info(s"Creating database directory=${dbPath.getAbsolutePath}")
      dbPath.mkdirs()
    } else {
      true
    }
  }
}

sealed abstract class MainNetDbConfig extends DbConfig {
  override lazy val configKey: String = "mainnetDb"
}

object MainNetDbConfig extends MainNetDbConfig

sealed abstract class TestNet3DbConfig extends DbConfig {
  override lazy val configKey: String = "testnet3Db"
}

object TestNet3DbConfig extends TestNet3DbConfig

sealed abstract class RegTestDbConfig extends DbConfig {
  override lazy val configKey: String = "regtestDb"
}

object RegTestDbConfig extends RegTestDbConfig

sealed abstract class UnitTestDbConfig extends DbConfig {
  override lazy val configKey: String = "unittestDb"

}

object UnitTestDbConfig extends UnitTestDbConfig
