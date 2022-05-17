package com.softwaremill.adopttapir

import com.softwaremill.adopttapir.config.Config
import com.softwaremill.quicklens._

import scala.concurrent.duration._

package object test {
  val DefaultConfig: Config = Config.read
  val TestConfig: Config = DefaultConfig
}
