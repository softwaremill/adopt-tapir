package com.softwaremill.adopttapir.starter.content

import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.{Setup, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectGenerator
import com.softwaremill.adopttapir.test.BaseTest
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.metrics.Metrics
class ContentServiceTest extends BaseTest:

  object ContentServiceTest:
    val service: Resource[IO, ContentService] =
      val sc = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
      val metrics = Metrics.noop
      GeneratedFilesFormatter.create(FilesManager(sc)).map(ContentService(_)(using metrics))

  import ContentServiceTest._

  it should "generate project tree for every valid configuration" in {
    allStarterDetails().foreach(sd => {
      service.use(_.generateContentTree(sd))
      // The content tree generation failure results in the exception being thrown and as a result a test failure
    })
  }

  private def allStarterDetails(): Seq[StarterDetails] = Setup.validConfigurations
