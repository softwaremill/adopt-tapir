package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.StarterDetails.defaultTapirVersion
import com.softwaremill.adopttapir.template.ProjectTemplate
import com.softwaremill.adopttapir.test.BaseTest

class StarterServiceITSpec extends BaseTest {

  it should "return zip file containing working sbt folder with Future Akka implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with Future Netty implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with IO Netty implementation" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, documentationAdded = false)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with Future Akka implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Akka, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with Future Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", FutureEffect, Netty, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with IO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Http4s, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with IO Netty implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails = StarterDetails("projectName", "com.softwaremill", IOEffect, Netty, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with ZIO Http4s implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, Http4s, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  it should "return zip file containing working sbt folder with ZIO ZIOHttp implementation with doc endpoint" in {
    val service = createStarterService

    val starterDetails =
      StarterDetails("projectName", "com.softwaremill", ZIOEffect, ZIOHttp, defaultTapirVersion, documentationAdded = true)

    IO.blocking(BFile.newTemporaryDirectory("sbtTesting"))
      .bracket { tempDir =>
        for {
          zipFile <- service.generateZipFile(starterDetails).map(_.toScala)
          _ <- IO.blocking {
            zipFile.unzipTo(tempDir)
            zipFile.delete()
          }
          _ <- IO.blocking(os.proc("sbt", ";compile ;test").call(cwd = os.Path(tempDir.toJava)).exitCode shouldBe 0)

        } yield zipFile

      }(tempDir => IO.blocking(tempDir.delete()))
      .unsafeRunSync()
  }

  private def createStarterService = {
    val config = StarterConfig(deleteTempFolder = true, tempPrefix = "sbtService", sbtVersion = "1.6.2", scalaVersion = "2.13.8")
    new StarterService(config, new ProjectTemplate(config))
  }

}
