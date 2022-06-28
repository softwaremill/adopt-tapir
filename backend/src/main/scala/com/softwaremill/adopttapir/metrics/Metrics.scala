package com.softwaremill.adopttapir.metrics

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, ServerImplementation, StarterDetails}
import io.prometheus.client.{Counter, hotspot}

object Metrics {
  lazy val generatedStarterCounter: Counter =
    Counter
      .build()
      .name(s"adopt_tapir_starter_generated_total")
      .labelNames(starterDetailsLabels: _*)
      .help(
        s"""Total generated starters with given parameters ${starterDetailsLabels.map(n => s"\"$n\"").mkString(", ")}"""
      )
      .register()

  def init(): Unit = hotspot.DefaultExports.initialize()

  val excludedStarterDetailsFields: Set[String] = Set("projectName", "groupId")

  private val starterDetailsLabels: Array[String] = {
    val fakeInstance: StarterDetails =
      StarterDetails("", "", ServerEffect.IOEffect, ServerImplementation.Akka, "", true, JsonImplementation.WithoutJson)

    val names = fakeInstance.productElementNames.toArray.filterNot(excludedStarterDetailsFields.contains)
    require(
      names.length == fakeInstance.productElementNames.length - excludedStarterDetailsFields.size,
      s"One of fields $excludedStarterDetailsFields no longer exists in ${fakeInstance.productElementNames.toList
          .mkString("StarterDetails(", ",", ")")}"
    )

    names
  }
}
