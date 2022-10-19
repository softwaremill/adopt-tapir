//package com.softwaremill.adopttapir.config
//
//import cats.data.NonEmptyList
//import cats.data.Reader
//import cats.effect.IO
//import com.softwaremill.adopttapir.config.Config
//import com.softwaremill.adopttapir.http.{Http, HttpApi}
//import com.softwaremill.adopttapir.metrics.VersionApi
//import com.softwaremill.adopttapir.starter.StarterService
//import com.softwaremill.adopttapir.starter.api.StarterApi
//import com.softwaremill.adopttapir.starter.content.ContentService
//import com.softwaremill.adopttapir.starter.files.FilesManager
//import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
//import com.softwaremill.adopttapir.template.ProjectGenerator
//import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
//
//type ConfigReader[A] = Reader[Config, A]
//
//object ConfigReader:
//
//  val generatedFilesFormatterReader: ConfigReader[GeneratedFilesFormatter] =
//    Reader(config => GeneratedFilesFormatter(config.storageConfig))
//
//  val starterServiceReader: ConfigReader[StarterService] =
//    generatedFilesFormatterReader.map(gff => StarterService(gff))
//
//  val contentServiceReader: ConfigReader[ContentService] =
//    generatedFilesFormatterReader.map(gff => ContentService(gff))
//
//  def starterApiReader(http: Http): ConfigReader[StarterApi] =
//    for
//      starterService <- starterServiceReader
//      contentService <- contentServiceReader
//    yield StarterApi(http, starterService, contentService)
//
//  def httpApiReader(http: Http, prometheusMetrics: PrometheusMetrics[IO]): ConfigReader[HttpApi] =
//    for
//      httpConfig <- Reader((config: Config) => config.api)
//      starterApi <- starterApiReader(http)
//      versionApi = VersionApi(http)
//    yield HttpApi(http, starterApi.endpoints, NonEmptyList.of(versionApi.versionEndpoint), prometheusMetrics, httpConfig)
//
//
//
//
