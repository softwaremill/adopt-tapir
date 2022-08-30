package com.softwaremill.adopttapir.starter.content

import com.softwaremill.adopttapir.starter.{Setup, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectGenerator
import com.softwaremill.adopttapir.test.BaseTest

class ContentServiceTest extends BaseTest {

  object ContentServiceTest {
    val service: ContentService = {
      new ContentService(new ProjectGenerator())
    }
  }

  it should "generate project tree for every valid configuration" in {
    allStarterDetails().foreach(sd => {
      ContentServiceTest.service.generateContentTree(sd)
      // if it would throw, the test would fail
    })
  }

  private def allStarterDetails(): Seq[StarterDetails] = Setup.validConfigurations
}
