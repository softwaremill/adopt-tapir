package com.softwaremill.adopttapir.starter

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

@deprecated("Only for development purpose")
object FileOperation extends IOApp:

  val createService: Resource[IO, StarterService] = for {
    given CorrelationId <- Resource.eval(CorrelationId.init)
    cfg <- Resource.eval(Config.read.map(_.storageConfig.copy(deleteTempFolder = false)))
    fm = FilesManager(cfg)
    given Metrics <- Resource.eval(Metrics.init())
    formatter <- GeneratedFilesFormatter.create(fm)
  } yield StarterService(formatter, fm)

  override def run(args: List[String]): IO[ExitCode] = {
    val details = StarterDetails(
      "amadeusz",
      "com.mjoyit.experience",
      ServerEffect.ZIOEffect,
      ServerImplementation.ZIOHttp,
      addDocumentation = true,
      addMetrics = false,
      JsonImplementation.Circe,
      ScalaVersion.Scala2,
      Builder.ScalaCli
    )

    createService.use(service =>
      for
        file <- service.generateZipFile(details)
        _ <- IO.println {
          val str = file.toString
          val index = str.lastIndexOf('_')
          "Directory: " + str.substring(0, index) + "\n" +
            "Zipped file: " + str
        }
        exitCode <- IO(ExitCode.Success)
      yield exitCode
    )
  }
