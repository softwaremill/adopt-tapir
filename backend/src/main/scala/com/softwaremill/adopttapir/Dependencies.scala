package com.softwaremill.adopttapir

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.softwaremill.adopttapir.config.{Config, ConfigReader}
import com.softwaremill.adopttapir.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.adopttapir.metrics.VersionApi
import com.softwaremill.adopttapir.starter.api.StarterApi
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

case class Dependencies(api: HttpApi)

object Dependencies {
  def wire(
             config: Config
          ): Resource[IO, Dependencies] =
    val prometheusMetrics = PrometheusMetrics.default[IO]("adopt_tapir")
    val http = Http()
    val httpApi = ConfigReader.httpApiReader(http, prometheusMetrics).run(config)
    
    Resource
      .make(Dependencies(httpApi).pure[IO])(_ => IO.pure(()))
}
