package com.softwaremill.adopttapir

import cats.data.{NonEmptyList, Reader, ReaderT}
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.adopttapir.metrics.{Metrics, VersionApi}
import com.softwaremill.adopttapir.starter.StarterService
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.api.StarterApi
import com.softwaremill.adopttapir.starter.content.ContentService
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

final case class Dependencies(api: HttpApi)

object Dependencies:

  private type ConfigReader[A] = ReaderT[Resource[IO, *], Config, A]

  private val filesManagerReader: ConfigReader[FilesManager] =
    Reader((config: Config) => FilesManager(config.storageConfig)).lift[Resource[IO, *]]

  private val generatedFilesFormatterReader: ConfigReader[GeneratedFilesFormatter] =
    filesManagerReader.flatMapF(fm => GeneratedFilesFormatter.create(fm))

  private def starterApiReader(http: Http)(using Metrics): ConfigReader[StarterApi] =
    val starterServiceReader: ConfigReader[StarterService] =
      for
        fm <- filesManagerReader
        gff <- generatedFilesFormatterReader
      yield StarterService(gff, fm)

    val contentServiceReader: ConfigReader[ContentService] =
      generatedFilesFormatterReader.map(gff => ContentService(gff))

    for
      starterService <- starterServiceReader
      contentService <- contentServiceReader
    yield StarterApi(http, starterService, contentService)

  private def httpApiReader(http: Http, prometheusMetrics: PrometheusMetrics[IO])(using Metrics): ConfigReader[HttpApi] =
    for
      httpConfig <- Reader((config: Config) => config.api).lift[Resource[IO, *]]
      starterApi <- starterApiReader(http)
      versionApi = VersionApi(http)
    yield HttpApi(http, starterApi.endpoints, NonEmptyList.of(versionApi.versionEndpoint), prometheusMetrics, httpConfig)

  def wire(
      config: Config
  ): Resource[IO, Dependencies] =
    val prometheusMetrics = PrometheusMetrics.default[IO]("adopt_tapir")
    val http = Http()
    for
      given Metrics <- Resource.eval(Metrics.init())
      httpApi <- httpApiReader(http, prometheusMetrics).run(config)
    yield Dependencies(httpApi)
