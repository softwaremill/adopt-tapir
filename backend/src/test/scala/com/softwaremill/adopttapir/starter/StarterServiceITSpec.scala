package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.StarterDetails.defaultTapirVersion
import com.softwaremill.adopttapir.template.ProjectTemplate
import com.softwaremill.adopttapir.test.BaseTest

import java.io.File

class StarterServiceITSpec extends BaseTest {

  it should "return zip file containing working sbt folder with Future Akka implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with Future Netty implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with IO Netty implementation" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = false)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with Future Akka implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, addDocumentation = true)

    checkZipped(service.generateZipFile(starterDetails)) { tempDir =>
      os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir)).exitCode shouldBe 0
    }
  }

  private def createStarterService = {
    val config = StarterConfig(deleteTempFolder = true, tempPrefix = "sbtService", sbtVersion = "1.6.2", scalaVersion = "2.13.8")
    new StarterService(config, new ProjectTemplate(config))
  }

  private def checkZipped[A](file: IO[File])(examineDirFn: File => A): Unit = {
    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- file.map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(examineDirFn(tempDir.toJava))

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

}
