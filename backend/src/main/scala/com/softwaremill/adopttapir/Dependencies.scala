package com.softwaremill.adopttapir

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.softwaremill.adopttapir.config.{Config, ConfigReader}
import com.softwaremill.adopttapir.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.adopttapir.metrics.VersionApi
import com.softwaremill.adopttapir.starter.api.StarterApi
//import com.softwaremill.macwire.autocats.autowire
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

case class Dependencies(api: HttpApi)

object Dependencies {
//  def wire(
//      config: Config
//  ): Resource[IO, Dependencies] = {
//    def buildHttpApi(
//        http: Http,
//        userApi: StarterApi,
//        versionApi: VersionApi,
//        cfg: HttpConfig
//    ) = {
//      val prometheusMetrics = PrometheusMetrics.default[IO]("adopt_tapir")
//      new HttpApi(
//        http,
//        userApi.endpoints,
//        NonEmptyList.of(versionApi.versionEndpoint),
//        prometheusMetrics,
//        cfg
//      )
//    }
//
//    autowire[Dependencies](
//      config.api,
//      config.storageConfig,
//      buildHttpApi _
//    )
//  }

  def wire(
             config: Config
          ): Resource[IO, Dependencies] =
    val prometheusMetrics = PrometheusMetrics.default[IO]("adopt_tapir")
    val http = Http()
    val httpApi = ConfigReader.httpApiReader(http, prometheusMetrics).run(config)
    val resource = Resource.make(Dependencies(httpApi).pure[IO])(_ => IO.pure(()))
    
    resource
}
