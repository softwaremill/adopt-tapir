package com.softwaremill.adopttapir.metrics

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, ServerImplementation, StarterDetails}
import io.prometheus.client.{Counter, hotspot}

object Metrics {
  lazy val generatedStarterCounter: Counter =
    Counter
      .build()
      .name(s"adopt_tapir_starter_generated_total")
      .labelNames(starterDetailsFieldNames: _*)
      .help(
        s"""Total generated starters with given parameters ${starterDetailsFieldNames.map(n => s"\"$n\"").mkString(", ")}"""
      )
      .register()

  def init(): Unit = hotspot.DefaultExports.initialize()

  private val starterDetailsFieldNames: Array[String] = {
    val fakeInstance: StarterDetails =
      StarterDetails("", "", ServerEffect.IOEffect, ServerImplementation.Akka, "", true, JsonImplementation.WithoutJson)

    fakeInstance.productElementNames.toArray
  }
}
