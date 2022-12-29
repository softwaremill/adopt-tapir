package com.softwaremill.adopttapir.metrics

import cats.effect.IO
import com.softwaremill.adopttapir.starter.ScalaVersion.*
import com.softwaremill.adopttapir.starter.*
import io.prometheus.client.{Counter, hotspot}
import cats.syntax.all.*

trait Metrics {
  def increaseMetricCounter(details: StarterDetails, operation: String): IO[Unit]
}

private class LiveMetrics(generatedStarterCounter: Counter) extends Metrics:

  import Metrics._

  override def increaseMetricCounter(details: StarterDetails, operation: String): IO[Unit] =
    val labelValues = details.productElementNames
      .zip(details.productIterator.toList)
      .filterNot { case (name, _) => excludedStarterDetailsFields.contains(name) }
      .map(_._2.toString)
      .toList :+ operation

    IO(
      generatedStarterCounter
        .labels(labelValues: _*)
        .inc()
    )

end LiveMetrics

object Metrics:
  private[metrics] lazy val excludedStarterDetailsFields: Set[String] = Set("projectName", "groupId")
  private[metrics] lazy val additionalLabels = Array("operation")

  val noop: Metrics = new Metrics:
    override def increaseMetricCounter(details: StarterDetails, operation: String): IO[Unit] = IO.unit
  def init(): IO[Metrics] = for
    _ <- IO(hotspot.DefaultExports.initialize())
    starterCounter <- generateStarterCounter
  yield LiveMetrics(starterCounter)

  def increaseZipGenerationMetricCounter(details: StarterDetails)(using m: Metrics): IO[Unit] =
    m.increaseMetricCounter(details, "generate")

  def increasePreviewOperationMetricCounter(details: StarterDetails)(using m: Metrics): IO[Unit] =
    m.increaseMetricCounter(details, "preview")

  private lazy val starterDetailsLabels: IO[Array[String]] = {
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

    IO {
      fakeInstance.productElementNames.toArray.filterNot(excludedStarterDetailsFields.contains) :++ additionalLabels
    }.flatTap(labels =>
      IO.raiseError(
        IllegalArgumentException(
          s"One of fields $excludedStarterDetailsFields no longer exists in ${fakeInstance.productElementNames.toList
              .mkString("StarterDetails(", ",", ")")}"
        )
      ).unlessA(
        labels.length == fakeInstance.productElementNames.length - excludedStarterDetailsFields.size + additionalLabels.length
      )
    )
  }

  private lazy val generateStarterCounter: IO[Counter] = starterDetailsLabels.flatMap(labels =>
    IO(
      Counter
        .build()
        .name(s"adopt_tapir_starter_generated_total")
        .labelNames(labels: _*)
        .help(
          s"""Total generated starters with given parameters ${labels.map(n => s"\"$n\"").mkString(", ")}"""
        )
        .register()
    )
  )
