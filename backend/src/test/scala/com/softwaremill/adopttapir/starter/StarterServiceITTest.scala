package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.starter.StarterDetails.defaultTapirVersion
import com.softwaremill.adopttapir.starter.api._
import com.softwaremill.adopttapir.template.ProjectTemplate
import com.softwaremill.adopttapir.test.BaseTest
import org.scalatest.ParallelTestExecution

import java.io.File

class StarterServiceITTest extends BaseTest with ParallelTestExecution {

  for {
    effect <- EffectRequest.values
    server <- ServerImplementationRequest.values
    docs <- List(true, false)
    json <- JsonImplementationRequest.values
    starterRequest = StarterRequest("myproject", "com.softwaremill", effect, server, defaultTapirVersion, addDocumentation = docs, json)
    starterDetails <- FormValidator.validate(starterRequest).toSeq
  } {
    it should s"return zip file containing working sbt folder with: $effect/$server/docs=$docs/$json" in {
      val service = createStarterService
      sbtCompileTest(service.generateZipFile(starterDetails))
    }
  }

  private def createStarterService = {
    val config = StarterConfig(deleteTempFolder = true, tempPrefix = "sbtService", sbtVersion = "1.6.2", scalaVersion = "2.13.8")
    new StarterService(config, new ProjectTemplate(config))
  }

  private def sbtCompileTest(file: IO[File]): Unit = {
    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- file.map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path((tempDir.toJava))).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }
}
