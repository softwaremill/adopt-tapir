package com.softwaremill.adopttapir.util

import com.softwaremill.adopttapir.config.Config

trait BaseModule {
  def clock: Clock
  def config: Config
}
