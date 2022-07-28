package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File => BFile}
import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import cats.instances.list._
import cats.syntax.parallel._
import com.softwaremill.adopttapir.starter.api._
import com.softwaremill.adopttapir.template.ProjectTemplate
import com.softwaremill.adopttapir.test.BaseTest
import org.scalatest.{Assertion, Assertions, ParallelTestExecution}
import os.SubProcess
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend, UriContext, asStringAlways, basicRequest}

import java.io.{PrintWriter, StringWriter}
import java.time.LocalDateTime
import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.TimeoutException
import scala.concurrent.duration.DurationInt
import scala.util.Using
import scala.util.matching.Regex

object Setup {
  val validConfigurations: Seq[StarterDetails] = for {
    effect <- EffectRequest.values
    server <- ServerImplementationRequest.values
    docs <- List(true, false)
    metrics <- List(true, false)
    json <- JsonImplementationRequest.values
    starterRequest = StarterRequest("myproject", "com.softwaremill", effect, server, addDocumentation = docs, addMetrics = metrics, json)
    starterDetails <- FormValidator.validate(starterRequest).toSeq
  } yield starterDetails

  implicit class StarterDetailsWithDescribe(details: StarterDetails) {
    lazy val describe: String =
      s"${details.serverEffect}/${details.serverImplementation}/docs=${details.addDocumentation}/metrics=${details.addMetrics}/${details.jsonImplementation}"
  }

  type TestFunction = Integer => IO[Assertion]
}

class StarterServiceITTest extends BaseTest with ParallelTestExecution {
  import Setup._

  for { details <- Setup.validConfigurations } {
    it should s"return zip file containing working sbt folder with: ${details.describe}" in {
      val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

      // define endpoints integration tests
      val helloEndpointTest: Option[TestFunction] = Some(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/hello?name=Frodo").send(backend).body

          result shouldBe "Hello Frodo"
        }
      )

      val docsEndpointTest: Option[TestFunction] = Option.when(details.addDocumentation)(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/docs/docs.yaml").send(backend).body

          result should include("paths:\n  /hello:")
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
        }
      )

      // get implementation for a given configuration, compile it, unit & integration test it
      val service = ServiceUnderTest(details)
      service.run(List(helloEndpointTest, docsEndpointTest, metricsEndpointTest).flatten)
    }
  }
}

case class ServiceUnderTest(details: StarterDetails) {
  import Setup._

  def run(tests: List[TestFunction]): Unit = {
    val log = new mutable.StringBuilder()
    (for {
      zipFile <- generateZipFile(details, log)
      tempDir <- createTempDirectory()
    } yield (zipFile, tempDir))
      .use { case (zipFile, tempDir) =>
        unzipFile(zipFile, tempDir, log) >> spawnService(tempDir).use(service =>
          getPortFromService(service, log).flatMap(port => runTests(port, tests, log))
        )
      }
      .unsafeRunSync()
  }

  private def generateZipFile(details: StarterDetails, log: mutable.StringBuilder): Resource[IO, BFile] = {
    Resource.make(for {
      zipFile <- ZipGenerator.service.generateZipFile(details).map(_.toScala)
      _ <- logStep(log, "* zip file was generated")
    } yield zipFile)(zipFile => IO.blocking(zipFile.delete()))
  }

  private def createTempDirectory(): Resource[IO, BFile] = {
    Resource.make(IO.blocking(BFile.newTemporaryDirectory("sbtTesting")))(tempDir => IO.blocking(tempDir.delete()))
  }

  private def unzipFile(zipFile: BFile, tempDir: BFile, log: mutable.StringBuilder): IO[Unit] = {
    IO.blocking(zipFile.unzipTo(tempDir)) >> logStep(log, "* zip file was unzipped")
  }

  private def spawnService(tempDir: BFile): Resource[IO, Service] = {
    Resource.make(IO.blocking(Service(tempDir)))(_.close())
  }

  private def getPortFromService(service: Service, log: mutable.StringBuilder): IO[Integer] = {
    for {
      port <- service.port
      _ <- logStep(log, s"* service compiled, tested & started on port $port")
    } yield port
  }

  private def runTests(port: Integer, tests: List[TestFunction], log: mutable.StringBuilder): IO[Unit] = {
    tests
      .map(_(port))
      .parSequence
      .timeoutAndForget(30.seconds)
      .onError(e =>
        Assertions.fail(
          s"Only the following test steps were finished for configuration '${details.describe}':$log\n${describe(e)}"
        )
      ) >> logStep(
      log,
      s"* integration tests on port $port were finished"
    )
  }

  private def logStep(steps: mutable.StringBuilder, step: String): IO[Unit] = IO { val _ = steps.append('\n').append(step) }

  private def describe(e: Throwable) = {
    Using.Manager { _ =>
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      e.printStackTrace(pw)
      sw.toString
    }.get
  }
}

case class Service(tempDir: better.files.File) {
  private val portPattern = new Regex("^(?:\\[info\\] )(?:Go to |Server started at )http://localhost:(\\d+).*")

  private val process: SubProcess = {
    os.proc(
      "sbt",
      "-no-colors",
      // start in forked mode so that process input can be forwarded and process waits before closing otherwise `StdIn.readLine` will exit immediately
      "set run / fork := true",
      // forward std input to forked process - https://www.scala-sbt.org/1.x/docs/Forking.html#Configuring+Input
      "set run / connectInput := true",
      ";compile ;test ;run"
    ).spawn(cwd = os.Path(tempDir.toJava), env = Map("http.port" -> "0"), mergeErrIntoOut = true)
  }

  val port: IO[Integer] = {
    val stdOut = new mutable.StringBuilder()
    val timeout = 60.seconds
    IO.blocking {
      val port = waitForPort(stdOut)
      assert(port > -1)
      port
    }.timeoutAndForget(timeout)
      .onError(e =>
        Assertions.fail(
          s"Detecting port of the running server failed${if (e.isInstanceOf[TimeoutException]) s" due to timeout [${timeout}s]" else ""} with process std output:\n$stdOut"
        )
      )
  }

  @tailrec
  private def waitForPort(stdOut: mutable.StringBuilder): Integer = {
    if (process.stdout.available() > 0 || process.isAlive()) {
      val line = process.stdout.readLine()
      if (line == null) {
        -1
      } else {
        stdOut.append("### process log <").append(new Timestamper).append(line).append(">").append('\n')
        portPattern.findFirstMatchIn(line) match {
          case Some(port) => port.group(1).toInt
          case None       => waitForPort(stdOut)
        }
      }
    } else {
      -1
    }
  }

  def close(): IO[Unit] = IO.blocking {
    if (process.isAlive()) process.close()
  }

  private class Timestamper {
    private val timestamp = LocalDateTime.now()

    override def toString: String = s"[$timestamp]"
  }
}

object ZipGenerator {
  val service: StarterService = {
    val config = StarterConfig(deleteTempFolder = true, tempPrefix = "sbtService", scalaVersion = "2.13.8")
    new StarterService(config, new ProjectTemplate(config))
  }
}
