package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.starter.JsonImplementation.{Circe, Jsoniter, WithoutJson, ZIOJson}
import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.StarterDetails.defaultTapirVersion
import com.softwaremill.adopttapir.template.ProjectTemplate
import com.softwaremill.adopttapir.test.BaseTest

import java.io.File

class StarterServiceITTest extends BaseTest {

  it should "return zip file containing working sbt folder with Future Akka implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = false, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Akka implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = true, WithoutJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  /*circe*/
  it should "return zip file containing working sbt folder with Future Akka implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = false, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Akka implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint with Circe Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = true, Circe)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }
  /*jsoniter*/
  it should "return zip file containing working sbt folder with Future Akka implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = false, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Akka implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint with Jsoniter Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = true, Jsoniter)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  /*zio-json*/
  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint with ZIO-json Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = true, ZIOJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint with ZIO-json Json" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = true, ZIOJson)

    sbtCompileTest(service.generateZipFile(starterDetails))
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
