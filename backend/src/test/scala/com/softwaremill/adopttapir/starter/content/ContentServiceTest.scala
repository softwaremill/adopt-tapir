package com.softwaremill.adopttapir.starter.content

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.Setup
import com.softwaremill.adopttapir.starter.files.{FilesManager, StorageConfig}
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.test.BaseTest
class ContentServiceTest extends BaseTest:

  object ContentServiceTest:
    val service: Resource[IO, ContentService] =
      val sc = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
      for
        given CorrelationId <- Resource.eval(CorrelationId.init)
        service <- GeneratedFilesFormatter.create(FilesManager(sc)).map(ContentService(_)(using Metrics.noop))
      yield service

  import ContentServiceTest.*

  it should "generate project tree for a valid configuration" in {
    // The content tree generation failure results in the exception being thrown and as a result a test failure
    // note that there is no point in re-testing all configurations generation as that gets validated in StartetServiceITTest
    service.use(_.generateContentTree(Setup.validConfigurations.head)).unsafeRunSync()
  }
