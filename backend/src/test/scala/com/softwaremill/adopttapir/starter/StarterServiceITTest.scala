package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import cats.instances.list._
import cats.syntax.parallel._
import com.softwaremill.adopttapir.starter.api._
import com.softwaremill.adopttapir.test.ServiceTimeouts.waitForPortTimeout
import com.softwaremill.adopttapir.test.{BaseTest, GeneratedService}
import org.scalatest.{Assertions, ParallelTestExecution}
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext, asStringAlways, basicRequest}

import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Properties

object Setup {
  lazy val jsonImplementations: List[JsonImplementationRequest] = {
    Properties
      .envOrElse("JSON", JsonImplementationRequest.values.mkString(","))
      .split(",")
      .map(JsonImplementationRequest.withName)
      .toList
  }

  lazy val scalaVersions: List[ScalaVersionRequest] = {
    Properties.envOrElse("SCALA", ScalaVersionRequest.values.mkString(",")).split(",").map(ScalaVersionRequest.withName).toList
  }

  val validConfigurations: Seq[StarterDetails] = for {
    effect <- EffectRequest.values
    server <- ServerImplementationRequest.values
    docs <- List(true, false)
    metrics <- List(true, false)
    json <- jsonImplementations
    scalaVersion <- scalaVersions
    builder <- BuilderRequest.values
    starterRequest = StarterRequest(
      "myproject",
      "com.softwaremill",
      effect,
      server,
      addDocumentation = docs,
      addMetrics = metrics,
      json,
      scalaVersion,
      builder
    )
    starterDetails <- FormValidator.validate(starterRequest).toSeq
  } yield starterDetails

  type TestFunction = Integer => IO[Unit]
}

object TestTimeouts {
  // wait for tests has to be longer than waiting for port otherwise it will break waiting for port with bogus errors
  val waitForTestsTimeout: FiniteDuration = waitForPortTimeout + 30.seconds
}

class StarterServiceITTest extends BaseTest with ParallelTestExecution {
  import Setup._
  import com.softwaremill.adopttapir.test.Describers.StarterDetailsWithDescribe

  for { details <- Setup.validConfigurations } {
    it should s"return zip file containing working sbt folder with: ${details.describe}" in {
      val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

      // define endpoints integration tests
      val helloEndpointTest: Option[TestFunction] = Some(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/hello?name=Frodo").send(backend).body

          result shouldBe "Hello Frodo"
          info(subTest("helo"))
        }
      )

      val docsEndpointTest: Option[TestFunction] = Option.when(details.addDocumentation)(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/docs/docs.yaml").send(backend).body

          result should include("paths:\n  /hello:")
          info(subTest("docs"))
        }
      )

      val metricsEndpointTest: Option[TestFunction] = Option.when(details.addMetrics)(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/metrics").send(backend).body

          result should (include("# HELP tapir_request_duration_seconds Duration of HTTP requests")
            and include("# TYPE tapir_request_duration_seconds histogram")
            and include("# HELP tapir_request_total Total HTTP requests")
            and include("# TYPE tapir_request_total counter")
            and include("# HELP tapir_request_active Active HTTP requests")
            and include("# TYPE tapir_request_active gauge"))
          info(subTest("metrics"))
        }
      )

      // get implementation for a given configuration, compile it, unit & integration test it
      val service = GeneratedServiceUnderTest(details)
      service.run(List(helloEndpointTest, docsEndpointTest, metricsEndpointTest).flatten)
    }
  }

  private def subTest(name: String): String = s"should have $name endpoint available"
}

case class GeneratedServiceUnderTest(details: StarterDetails) {
  import Setup._
  import TestTimeouts.waitForTestsTimeout
  import com.softwaremill.adopttapir.test.Describers._

  def run(tests: List[TestFunction]): Unit = {
    val logger = RunLogger()
    (for {
      zipFile <- generateZipFile(details, logger)
      tempDir <- createTempDirectory()
    } yield (zipFile, tempDir))
      .use { case (zipFile, tempDir) =>
        unzipFile(zipFile, tempDir, logger) >> spawnService(tempDir).use(service =>
          getPortFromService(service, logger).flatMap(port => runTests(port, tests, logger))
        )
      }
      .unsafeRunSync()
  }

  private def generateZipFile(details: StarterDetails, logger: RunLogger): Resource[IO, BFile] = {
    Resource.make(for {
      zipFile <- ZipGenerator.service.generateZipFile(details).map(_.toScala)
      _ <- logger.log("* zip file was generated")
    } yield zipFile)(zipFile => IO.blocking(zipFile.delete()))
  }

  private def createTempDirectory(): Resource[IO, BFile] = {
    Resource.make(IO.blocking(BFile.newTemporaryDirectory("sbtTesting")))(tempDir => IO.blocking(tempDir.delete(true)))
  }

  private def unzipFile(zipFile: BFile, tempDir: BFile, logger: RunLogger): IO[Unit] = {
    IO.blocking(zipFile.unzipTo(tempDir)) >> logger.log("* zip file was unzipped")
  }

  private def spawnService(tempDir: BFile): Resource[IO, GeneratedService] = {
    Resource.make(GeneratedService(details.builder, tempDir))(_.close())
  }

  private def getPortFromService(service: GeneratedService, logger: RunLogger): IO[Integer] = {
    for {
      port <- service.port
      _ <- logger.log(s"* service compiled, tested & started on port $port")
    } yield port
  }

  private def runTests(port: Integer, tests: List[TestFunction], logger: RunLogger): IO[Unit] = {
    tests
      .map(_(port))
      .parSequence
      .timeoutAndForget(waitForTestsTimeout)
      .onError(e =>
        Assertions.fail(
          s"Only the following test steps were finished for configuration '${details.describe}':$logger${System.lineSeparator()}${e.describe()}"
        )
      ) >> logger.log(s"* integration tests on port $port were finished")
  }
}

object ZipGenerator {
  val service: StarterService = {
    val config = StarterConfig(deleteTempFolder = true, tempPrefix = "generatedService")
    new StarterService(config)
  }
}

case class RunLogger(log: mutable.StringBuilder = new mutable.StringBuilder()) {
  def log(l: String): IO[Unit] = IO(log.append(System.lineSeparator()).append(l))
  override def toString: String = log.toString()
}
