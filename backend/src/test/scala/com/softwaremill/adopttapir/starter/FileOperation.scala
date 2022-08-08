package com.softwaremill.adopttapir.starter

import cats.effect.{ExitCode, IO, IOApp}
import com.softwaremill.adopttapir.config.Config

@deprecated("Only for development purpose")
object FileOperation extends IOApp {

  private val cfg = Config.read.starter
  val service = new StarterService(cfg.copy(deleteTempFolder = false))

  override def run(args: List[String]): IO[ExitCode] = {
    val details = StarterDetails(
      "amadeusz",
      "com.mjoyit.experience",
      ServerEffect.ZIOEffect,
      ServerImplementation.ZIOHttp,
      true,
      false,
      JsonImplementation.Circe,
      ScalaVersion.Scala2
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
