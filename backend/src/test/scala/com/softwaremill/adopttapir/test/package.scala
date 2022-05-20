package com.softwaremill.adopttapir

import com.softwaremill.adopttapir.config.Config

package object test {
  val DefaultConfig: Config = Config.read
  val TestConfig: Config = DefaultConfig
}
