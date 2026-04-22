package com.softwaremill.adopttapir.starter

import better.files.{FileExtensions, File as BFile}
import cats.effect.unsafe.implicits.global
import cats.effect.{Deferred, IO, Resource}
import cats.syntax.all.*
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.api.*
import com.softwaremill.adopttapir.starter.files.{FilesManager, StorageConfig}
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.test.ServiceTimeouts.waitForPortTimeout
import com.softwaremill.adopttapir.test.ShowHelpers.*
import com.softwaremill.adopttapir.test.{BaseTest, GeneratedService, OtlpMetricsSink, ServiceFactory}
import org.scalatest.{Assertions, ParallelTestExecution}
import sttp.client4.{DefaultSyncBackend, SyncBackend, UriContext, asStringAlways, basicRequest}

import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Properties

object Setup:
  trait TestFunction:
    def setup: Resource[IO, Map[String, String]] = Resource.pure(Map.empty)
    def apply(port: Integer): IO[Unit]

  object TestFunction:
    def apply(fn: Integer => IO[Unit]): TestFunction = (port: Integer) => fn(port)

  def fromEnvOrElseAll[A](envKeyString: String, fromEnv: String => A)(elseAll: List[A]): List[A] =
    Properties
      .envOrNone(envKeyString)
      .map { envValuesString =>
        envValuesString
          .split(",")
          .toList
          .map(fromEnv)
      }
      .getOrElse(elseAll)

  lazy val validConfigurations: Seq[StarterDetails] = for
    stack <- stackImplementations
    server <- ServerImplementationRequest.values.toIndexedSeq
    docs <- List(true, false)
    metrics <- List(true, false)
    json <- jsonImplementations
    scalaVersion <- scalaVersions
    builder <- BuilderRequest.values.toIndexedSeq
    starterRequest = StarterRequest(
      "myproject",
      "com.softwaremill",
      stack,
      server,
      addDocumentation = docs,
      addMetrics = metrics,
      json,
      scalaVersion,
      builder
    )
    starterDetails <- FormValidator.validate(starterRequest).toSeq
  yield starterDetails

  private lazy val jsonImplementations: List[JsonImplementationRequest] =
    fromEnvOrElseAll("JSON", JsonImplementationRequest.valueOf)(JsonImplementationRequest.values.toList)

  private lazy val scalaVersions: List[ScalaVersionRequest] =
    fromEnvOrElseAll("SCALA", ScalaVersionRequest.valueOf)(ScalaVersionRequest.values.toList)

  private lazy val stackImplementations: List[StackRequest] =
    fromEnvOrElseAll("STACK", StackRequest.valueOf)(StackRequest.values.toList)

object TestTimeouts:
  // wait for tests has to be longer than waiting for port otherwise it will break waiting for port with bogus errors
  val waitForTestsTimeout: FiniteDuration = waitForPortTimeout + 30.seconds

class StarterServiceITTest extends BaseTest with ParallelTestExecution:
  import Setup.*

  for details <- Setup.validConfigurations do {
    it should s"work using the configuration: ${details.show}" in {
      val backend: SyncBackend = DefaultSyncBackend()

      // define endpoints integration tests
      val helloEndpointTest: TestFunction = TestFunction(port =>
        IO.blocking {
          val result: String =
            basicRequest.response(asStringAlways).get(uri"http://localhost:$port/hello?name=Frodo").send(backend).body

          result shouldBe "Hello Frodo"
          info(subTest("hello endpoint"))
        }
      )

      val docsEndpointTest: Option[TestFunction] = Option.when(details.addDocumentation)(
        TestFunction(port =>
          IO.blocking {
            val result: String =
              basicRequest.response(asStringAlways).get(uri"http://localhost:$port/docs/docs.yaml").send(backend).body

            result should include("paths:\n  /hello:")
            info(subTest("docs endpoint"))
          }
        )
      )

      val metricsTest: Option[IO[TestFunction]] = Option.when(details.addMetrics)(
        Deferred[IO, OtlpMetricsSink].map { sink =>
          new TestFunction:
            override val setup: Resource[IO, Map[String, String]] =
              OtlpMetricsSink.resource.evalTap(sink.complete).map(otlpEnv)
            def apply(port: Integer): IO[Unit] =
              sink.get.flatMap(_.awaitFirstPush) >> IO(info(subTest("metrics")))
        }
      )

      val tests: IO[List[TestFunction]] =
        metricsTest.sequence.map(metricsTest => List(Some(helloEndpointTest), docsEndpointTest, metricsTest).flatten)

      // get implementation for a given configuration, compile it, unit & integration test it
      val service = GeneratedServiceUnderTest(new ServiceFactory, details)
      service.run(tests)
    }
  }

  private def subTest(name: String): String = s"should have $name available"

  private def otlpEnv(sink: OtlpMetricsSink): Map[String, String] = Map(
    "OTEL_TRACES_EXPORTER" -> "none",
    "OTEL_LOGS_EXPORTER" -> "none",
    "OTEL_METRICS_EXPORTER" -> "otlp",
    "OTEL_EXPORTER_OTLP_PROTOCOL" -> "http/protobuf",
    "OTEL_EXPORTER_OTLP_METRICS_ENDPOINT" -> s"http://localhost:${sink.port}/v1/metrics",
    "OTEL_METRIC_EXPORT_INTERVAL" -> "1000"
  )

case class GeneratedServiceUnderTest(serviceFactory: ServiceFactory, details: StarterDetails):
  import Setup.*
  import TestTimeouts.waitForTestsTimeout

  def run(testsIO: IO[List[TestFunction]]): Unit =
    val logger = RunLogger()
    testsIO
      .flatMap { tests =>
        (for
          zipFile <- generateZipFile(details, logger)
          tempDir <- createTempDirectory()
          env <- tests.traverse(_.setup)
        yield (zipFile, tempDir, env.combineAll))
          .use { case (zipFile, tempDir, env) =>
            unzipFile(zipFile, tempDir, logger) >> spawnService(tempDir, details.projectName, env)
              .use(service => getPortFromService(service, logger).flatMap(port => runTests(port, tests, logger)))
          }
      }
      .unsafeRunSync()

  private def generateZipFile(details: StarterDetails, logger: RunLogger): Resource[IO, BFile] =
    ZipGenerator.service.flatMap(generator =>
      Resource.make(for
        zipFile <- generator.generateZipFile(details).map(_.toScala)
        _ <- logger.log("* zip file was generated")
      yield zipFile)(zipFile => IO.blocking(zipFile.delete()))
    )

  private def createTempDirectory(): Resource[IO, BFile] =
    Resource.make(IO.blocking(BFile.newTemporaryDirectory("sbtTesting")))(tempDir =>
      IO.blocking(tempDir.delete(swallowIOExceptions = true))
    )

  private def unzipFile(zipFile: BFile, tempDir: BFile, logger: RunLogger): IO[Unit] =
    IO.blocking(zipFile.unzipTo(tempDir)) >> logger.log("* zip file was unzipped")

  private def spawnService(tempDir: BFile, projectName: String, env: Map[String, String]): Resource[IO, GeneratedService] =
    Resource.make(serviceFactory.create(details.builder, tempDir / projectName, env))(_.close())

  private def getPortFromService(service: GeneratedService, logger: RunLogger): IO[Integer] =
    for
      port <- service.port
      _ <- logger.log(s"* service compiled, tested & started on port $port")
    yield port

  private def runTests(port: Integer, tests: List[TestFunction], logger: RunLogger): IO[Unit] =
    tests
      .map(_(port))
      .parSequence
      .timeoutAndForget(waitForTestsTimeout)
      .onError(e =>
        Assertions.fail(
          s"Only the following test steps were finished for configuration '${details.show}':$logger${System.lineSeparator()}${e.show}"
        )
      ) >> logger.log(s"* integration tests on port $port were finished")

object ZipGenerator:
  val service: Resource[IO, StarterService] =
    val cfg = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
    val fm = new FilesManager(cfg)
    for
      given CorrelationId <- Resource.eval(CorrelationId.init)
      service <- GeneratedFilesFormatter.create(fm).map(StarterService(_, fm)(using Metrics.noop))
    yield service

case class RunLogger(log: mutable.StringBuilder = new mutable.StringBuilder()):
  def log(l: String): IO[Unit] = IO(log.append(System.lineSeparator()).append(l)).void
  override def toString: String = log.toString()
