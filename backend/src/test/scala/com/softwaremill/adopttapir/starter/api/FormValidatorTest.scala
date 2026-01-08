package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.JsonImplementation.WithoutJson
import com.softwaremill.adopttapir.starter.ScalaVersion.Scala2
import com.softwaremill.adopttapir.starter.api.StackRequest.{FutureStack, IOStack, ZIOStack}
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.No
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterRequestGenerators.randomStarterRequest
import com.softwaremill.adopttapir.starter.{Builder, ServerStack, ServerImplementation, StarterDetails}
import com.softwaremill.adopttapir.test.BaseTest

class FormValidatorTest extends BaseTest:

  "FormValidator" should "raise a problem with project name" in {
    // given
    val starterRequest = randomStarterRequest().copy(projectName = "UPPER project name")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include(
      s"Project name: `${starterRequest.projectName}` should match regex: `^[a-z0-9_]$$|^[a-z0-9_]+[a-z0-9_-]*[a-z0-9_]+$$`"
    )
  }

  it should "raise a problem with groupId when it not follow Java naming package" in {
    // given
    val starterRequest = randomStarterRequest().copy(groupId = "Upper.Name.Package")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include("GroupId: `Upper.Name.Package` should follow Java package convention")
  }

  it should "raise a problem with picking ZIOJson for other effect than ZIO" in {
    // given
    val requestWithFutureStack = randomStarterRequest().copy(json = JsonImplementationRequest.ZIOJson, stack = StackRequest.FutureStack)
    val requestWithIOStack = randomStarterRequest().copy(json = JsonImplementationRequest.ZIOJson, stack = StackRequest.FutureStack)

    // when
    val resultFuture = FormValidator.validate(requestWithFutureStack)
    val resultIO = FormValidator.validate(requestWithIOStack)

    // then
    resultFuture.left.value.msg should include(s"ZIOJson will work only with ZIO stack")
    resultIO.left.value.msg should include(s"ZIOJson will work only with ZIO stack")

  }

  it should "raise a problem when stack will not match implementation" in {
    FormValidator.validate(randomStarterRequest(FutureStack, ZIOHttp)).left.value.msg should
      include("Picked FutureStack with ZIOHttp - Future stack will work only with: Netty, Vert.X, Pekko")
    FormValidator.validate(randomStarterRequest(FutureStack, Http4s)).left.value.msg should
      include("Picked FutureStack with Http4s - Future stack will work only with: Netty, Vert.X, Pekko")
    FormValidator.validate(randomStarterRequest(IOStack, ZIOHttp)).left.value.msg should
      include("Picked IOStack with ZIOHttp - IO stack will work only with: Netty, Vert.X, Http4s")
  }

  it should "not raise a problem with Effect and Implementation" in {
    val request = defaultRequest()
    val request2 = request.copy(stack = IOStack, implementation = Netty)
    val request3 = request.copy(stack = IOStack, implementation = Http4s)
    val request4 = request.copy(stack = ZIOStack, implementation = Http4s)
    val request5 = request.copy(stack = ZIOStack, implementation = ZIOHttp)

    FormValidator.validate(request).value shouldBe StarterDetails(
      request.projectName,
      request.groupId,
      ServerStack.FutureStack,
      ServerImplementation.Netty,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
    FormValidator.validate(request2).value shouldBe StarterDetails(
      request2.projectName,
      request2.groupId,
      ServerStack.IOStack,
      ServerImplementation.Netty,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
    FormValidator.validate(request3).value shouldBe StarterDetails(
      request3.projectName,
      request3.groupId,
      ServerStack.IOStack,
      ServerImplementation.Http4s,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
    FormValidator.validate(request4).value shouldBe StarterDetails(
      request4.projectName,
      request4.groupId,
      ServerStack.ZIOStack,
      ServerImplementation.Http4s,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
    FormValidator.validate(request5).value shouldBe StarterDetails(
      request5.projectName,
      request5.groupId,
      ServerStack.ZIOStack,
      ServerImplementation.ZIOHttp,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
  }

  private def defaultRequest(): StarterRequest =
    StarterRequest(
      "project",
      "com.softwaremill",
      FutureStack,
      Netty,
      addDocumentation = true,
      addMetrics = false,
      No,
      ScalaVersionRequest.Scala2,
      BuilderRequest.Sbt
    )
