package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, ServerImplementation, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectTemplate.legalizeGroupId
import com.softwaremill.adopttapir.template.ProjectTemplateTest.createStarterDetails
import com.softwaremill.adopttapir.test.BaseTest

class ProjectTemplateTest extends BaseTest {

  it should "pass legal groupId as it is" in {
    legalizeGroupId(createStarterDetails(groupId = "com.softwaremill")).groupId shouldBe "com.softwaremill"
  }

  it should "add underscore when any of packages starts with digit" in {
    legalizeGroupId(createStarterDetails(groupId = "com.1softwaremill")).groupId shouldBe "com._1softwaremill"
    legalizeGroupId(createStarterDetails(groupId = "1com.softwaremill")).groupId shouldBe "_1com.softwaremill"
  }
}

object ProjectTemplateTest {
  def createStarterDetails(groupId: String) = StarterDetails(
    "projectName",
    groupId,
    ServerEffect.ZIOEffect,
    ServerImplementation.ZIOHttp,
    "1.0.0",
    false,
    false,
    JsonImplementation.WithoutJson
  )
}
