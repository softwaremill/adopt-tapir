package com.softwaremill.adopttapir.starter.content

import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.{Setup, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectGenerator
import com.softwaremill.adopttapir.test.BaseTest

class ContentServiceTest extends BaseTest:

  object ContentServiceTest:
    val service: ContentService =
      val sc = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
      val pf = new GeneratedFilesFormatter(FilesManager(sc))
      new ContentService(pf)

  import ContentServiceTest._

  it should "generate project tree for every valid configuration" in {
    allStarterDetails().foreach(sd => {
      service.generateContentTree(sd)
      // The content tree generation failure results in the exception being thrown and as a result a test failure
    })
  }

  private def allStarterDetails(): Seq[StarterDetails] = Setup.validConfigurations
