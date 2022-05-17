package com.softwaremill.adopttapir.metrics

import io.prometheus.client.{Counter, hotspot}

object Metrics {
  lazy val registeredUsersCounter: Counter =
    Counter
      .build()
      .name(s"adopttapir_registered_users_counter")
      .help(s"How many users registered on this instance since it was started")
      .register()

  def init(): Unit = {
    hotspot.DefaultExports.initialize()
  }
}
