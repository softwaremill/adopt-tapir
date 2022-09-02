package com.softwaremill.adopttapir.starter.content

import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.starter.formatting.ProjectFormatter
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.{Setup, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectGenerator
import com.softwaremill.adopttapir.test.BaseTest

class ContentServiceTest extends BaseTest {

  object ContentServiceTest {
    val service: ContentService = {
      val pg = new ProjectGenerator()
      val sc = StorageConfig(deleteTempFolder = true, tempPrefix = "generatedService")
      val fm = new FilesManager(sc)
      val pf = new ProjectFormatter(fm)
      new ContentService(pg, pf)
    }
  }

  import ContentServiceTest._

  it should "generate project tree for every valid configuration" in {
    allStarterDetails().foreach(sd => {
      service.generateContentTree(sd)
      // if it would throw, the test would fail
    })
  }

  private def allStarterDetails(): Seq[StarterDetails] = Setup.validConfigurations
}
