package com.softwaremill.adopttapir.starter

import cats.effect.{ExitCode, IO, IOApp}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.starter.StarterDetails.{FutureStarterDetails, IOStarterDetails, ZIOStarterDetails, defaultTapirVersion}
import com.softwaremill.adopttapir.template.ProjectTemplate

@deprecated("Only for development purpose")
object FileOperation extends IOApp {

  private val cfg = Config.read.starter
  val service = new StarterService(null, cfg.copy(deleteTempFolder = false), new ProjectTemplate(cfg))

  override def run(args: List[String]): IO[ExitCode] = {
    val details = FutureStarterDetails(
      "amadeusz",
      "com.mjoyit.experience",
      ServerImplementation.Netty,
      defaultTapirVersion
    )

    for {
      file <- service.generateZipFile(details)
      _ <- IO.println {
        val str = file.toString
        val index = str.lastIndexOf('_')
        "Directory: " + str.substring(0, index) + "\n" +
          "Zipped file: " + str
      }
      exitCode <- IO(ExitCode.Success)
    } yield exitCode
  }
}
