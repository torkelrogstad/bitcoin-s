package org.bitcoins.core.util

object FileUtil {

  def getFileAsSource(fileName: String): scala.io.BufferedSource = {
    scala.io.Source.fromURL(getClass.getResource(s"/$fileName"))
  }
}
