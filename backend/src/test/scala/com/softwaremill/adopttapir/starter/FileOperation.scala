package com.softwaremill.adopttapir.starter

import cats.effect.{ExitCode, IO, IOApp}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

@deprecated("Only for development purpose")
object FileOperation extends IOApp {

  val service: StarterService = {
    val cfg = Config.read.storageConfig.copy(deleteTempFolder = false)
    val fm = new FilesManager(cfg)
//    val gff = GeneratedFilesFormatter(fm)
    new StarterService(GeneratedFilesFormatter(fm))
  }

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
  }
}
