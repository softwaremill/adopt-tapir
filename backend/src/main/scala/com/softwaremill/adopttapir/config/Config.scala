package com.softwaremill.adopttapir.config

import com.softwaremill.adopttapir.http.HttpConfig
import com.softwaremill.adopttapir.infrastructure.DBConfig
import com.softwaremill.adopttapir.starter.StarterConfig
import com.softwaremill.adopttapir.version.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

/** Maps to the `application.conf` file. Configuration for all modules of the application. */
case class Config(db: DBConfig, api: HttpConfig, starter: StarterConfig)

object Config extends StrictLogging {
  def log(config: Config): Unit = {
    val baseInfo = s"""
                      |Adopt-tapir configuration:
                      |-----------------------
                      |DB:             ${config.db}
                      |API:            ${config.api}
                      |Starter:        ${config.starter}
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
