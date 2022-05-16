package com.softwaremill.adopttapir

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.adopttapir.metrics.VersionApi
import com.softwaremill.adopttapir.starter.StarterApi
import com.softwaremill.adopttapir.util.{Clock, DefaultIdGenerator}
import com.softwaremill.macwire.autocats.autowire
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

case class Dependencies(api: HttpApi)

object Dependencies {
  def wire(
      config: Config,
      xa: Resource[IO, Transactor[IO]],
      clock: Clock
  ): Resource[IO, Dependencies] = {
    def buildHttpApi(
        http: Http,
        userApi: StarterApi,
        versionApi: VersionApi,
        cfg: HttpConfig
    ) = {
      val prometheusMetrics = PrometheusMetrics.default[IO](registry = new CollectorRegistry())
      new HttpApi(
        http,
        userApi.endpoints,
        NonEmptyList.of(versionApi.versionEndpoint),
        prometheusMetrics,
        cfg
      )
    }

    autowire[Dependencies](
      config.api,
      config.starter,
      DefaultIdGenerator,
      clock,
      xa,
      buildHttpApi _,
    )
  }
}
