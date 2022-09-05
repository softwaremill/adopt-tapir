package com.softwaremill.adopttapir.config

import com.softwaremill.adopttapir.http.HttpConfig
import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.version.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

/** Maps to the `application.conf` file. Configuration for all modules of the application. */
case class Config(api: HttpConfig, storageConfig: StorageConfig)

object Config extends StrictLogging {
  def log(config: Config): Unit = {
    val baseInfo = s"""
                      |Adopt-tapir configuration:
                      |-----------------------
                      |API:            ${config.api}
                      |Storage:        ${config.storageConfig}
                      |
                      |Build & env info:
                      |-----------------
                      |""".stripMargin

    val info = TreeMap(BuildInfo.toMap.toSeq: _*).foldLeft(baseInfo) { case (str, (k, v)) =>
      str + s"$k: $v\n"
    }

    logger.info(info)
  }

  def read: Config = ConfigSource.default.loadOrThrow[Config]
}
