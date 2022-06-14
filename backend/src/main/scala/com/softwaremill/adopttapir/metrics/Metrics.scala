package com.softwaremill.adopttapir.metrics

import io.prometheus.client.Counter

object Metrics {
  lazy val registeredUsersCounter: Counter =
    Counter
      .build()
      .name(s"adopttapir_registered_users_counter")
      .help(s"How many users registered on this instance since it was started")
      .register()
}
