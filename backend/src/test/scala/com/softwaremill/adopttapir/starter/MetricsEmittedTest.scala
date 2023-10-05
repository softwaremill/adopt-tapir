package com.softwaremill.adopttapir.starter

import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.content.ContentService
import com.softwaremill.adopttapir.starter.files.{FilesManager, StorageConfig}
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.test.BaseTest
import org.scalatest.Assertion

class MetricsEmittedTest extends BaseTest:
  import MetricsEmittedTest.*
  import cats.effect.unsafe.implicits.global

  it should "emit 'preview' metric when preview service is called" in withMetricsService { metrics =>
    val contentService: Resource[IO, ContentService] =
      val sc = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
      for
        given CorrelationId <- Resource.eval(CorrelationId.init)
        service <- GeneratedFilesFormatter.create(FilesManager(sc)).map(ContentService(_)(using metrics))
      yield service

    contentService.use(_.generateContentTree(Setup.validConfigurations.head)).unsafeRunSync()

    metrics.called should have size 1
    metrics.called.head should equal("preview")
  }

  it should "emit 'generate' metric when starter service is called" in withMetricsService { metrics =>
    val starterService: Resource[IO, StarterService] =
      val fm = new FilesManager(StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService"))
      for
        given CorrelationId <- Resource.eval(CorrelationId.init)
        service <- GeneratedFilesFormatter.create(fm).map(StarterService(_, fm)(using metrics))
      yield service

    starterService.use(_.generateZipFile(Setup.validConfigurations.head)).unsafeRunSync()

    metrics.called should have size 1
    metrics.called.head should equal("generate")
  }

object MetricsEmittedTest:
  def withMetricsService(testCode: (TestMetrics) => Assertion): Assertion =
    testCode(new TestMetrics)

  class TestMetrics extends Metrics:
    var called: List[String] = List.empty

    override def increaseMetricCounter(details: StarterDetails, operation: String): IO[Unit] =
      IO.blocking { called = called :+ operation }

end MetricsEmittedTest
