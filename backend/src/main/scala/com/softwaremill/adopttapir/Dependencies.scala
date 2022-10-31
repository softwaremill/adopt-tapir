package com.softwaremill.adopttapir

import cats.data.{NonEmptyList, Reader}
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.adopttapir.metrics.VersionApi
import com.softwaremill.adopttapir.starter.StarterService
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.api.StarterApi
import com.softwaremill.adopttapir.starter.content.ContentService
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

final case class Dependencies(api: HttpApi)

object Dependencies:

  private type ConfigReader[A] = Reader[Config, A]

  private val filesManagerReader: ConfigReader[FilesManager] =
    Reader(config => FilesManager(config.storageConfig))

  private val generatedFilesFormatterReader: ConfigReader[GeneratedFilesFormatter] =
    filesManagerReader.map(fm => GeneratedFilesFormatter(fm))

  private val starterServiceReader: ConfigReader[StarterService] =
    for
      fm <- filesManagerReader
      gff <- generatedFilesFormatterReader
    yield StarterService(gff, fm)

  private val contentServiceReader: ConfigReader[ContentService] =
    generatedFilesFormatterReader.map(gff => ContentService(gff))

  private def starterApiReader(http: Http): ConfigReader[StarterApi] =
    for
      starterService <- starterServiceReader
      contentService <- contentServiceReader
    yield StarterApi(http, starterService, contentService)

  private def httpApiReader(http: Http, prometheusMetrics: PrometheusMetrics[IO]): ConfigReader[HttpApi] =
    for
      httpConfig <- Reader((config: Config) => config.api)
      starterApi <- starterApiReader(http)
      versionApi = VersionApi(http)
    yield HttpApi(http, starterApi.endpoints, NonEmptyList.of(versionApi.versionEndpoint), prometheusMetrics, httpConfig)

  def wire(
      config: Config
  ): Resource[IO, Dependencies] =
    val prometheusMetrics = PrometheusMetrics.default[IO]("adopt_tapir")
    val http = Http()
    val httpApi = httpApiReader(http, prometheusMetrics).run(config)

    Resource
      .make(Dependencies(httpApi).pure[IO])(_ => IO.pure(()))
