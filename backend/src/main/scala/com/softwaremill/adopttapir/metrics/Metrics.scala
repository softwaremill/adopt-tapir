package com.softwaremill.adopttapir.metrics

import com.softwaremill.adopttapir.starter.ScalaVersion._
import com.softwaremill.adopttapir.starter._
import io.prometheus.client.{Counter, hotspot}

object Metrics {
  def init(): Unit = hotspot.DefaultExports.initialize()

  def increaseZipGenerationMetricCounter(details: StarterDetails): Unit = {
    increaseMetricCounter(details, "generate")
  }

  def increasePreviewOperationMetricCounter(details: StarterDetails): Unit = {
    increaseMetricCounter(details, "preview")
  }

  private def increaseMetricCounter(details: StarterDetails, operation: String): Unit = {
    val labelValues = details.productElementNames
      .zip(details.productIterator.toList)
      .filterNot { case (name, _) => excludedStarterDetailsFields.contains(name) }
      .map(_._2.toString)
      .toList :+ operation

    generatedStarterCounter
      .labels(labelValues: _*)
      .inc()
  }

  private lazy val generatedStarterCounter: Counter =
    Counter
      .build()
      .name(s"adopt_tapir_starter_generated_total")
      .labelNames(starterDetailsLabels: _*)
      .help(
        s"""Total generated starters with given parameters ${starterDetailsLabels.map(n => s"\"$n\"").mkString(", ")}"""
      )
      .register()

  private lazy val excludedStarterDetailsFields: Set[String] = Set("projectName", "groupId")
  private lazy val additionalLabels = Array("operation")

  private lazy val starterDetailsLabels: Array[String] = {
    val fakeInstance: StarterDetails =
      StarterDetails(
        "",
        "",
        ServerEffect.IOEffect,
        ServerImplementation.Netty,
        true,
        false,
        JsonImplementation.WithoutJson,
        Scala2,
        Builder.Sbt
      )

    val labels = fakeInstance.productElementNames.toArray.filterNot(excludedStarterDetailsFields.contains) :++ additionalLabels
    require(
      labels.length == fakeInstance.productElementNames.length - excludedStarterDetailsFields.size + additionalLabels.length,
      s"One of fields $excludedStarterDetailsFields no longer exists in ${fakeInstance.productElementNames.toList
          .mkString("StarterDetails(", ",", ")")}"
    )

    labels
  }
}
