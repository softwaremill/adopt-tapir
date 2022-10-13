package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.JsonImplementation.WithoutJson
import com.softwaremill.adopttapir.starter.ScalaVersion.Scala2
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.No
import com.softwaremill.adopttapir.starter.api.ScalaVersionRequest.Scala3
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterRequestGenerators.randomStarterRequest
import com.softwaremill.adopttapir.starter.{Builder, ServerEffect, ServerImplementation, StarterDetails}
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
    val requestWithFutureEffect = randomStarterRequest().copy(json = JsonImplementationRequest.ZIOJson, effect = EffectRequest.FutureEffect)
    val requestWithIOEffect = randomStarterRequest().copy(json = JsonImplementationRequest.ZIOJson, effect = EffectRequest.FutureEffect)

    // when
    val resultFuture = FormValidator.validate(requestWithFutureEffect)
    val resultIO = FormValidator.validate(requestWithIOEffect)

    // then
    resultFuture.left.value.msg should include(s"ZIOJson will work only with ZIO effect")
    resultIO.left.value.msg should include(s"ZIOJson will work only with ZIO effect")

  }

  it should "raise a problem when effect will not match implementation" in {
    FormValidator.validate(randomStarterRequest(FutureEffect, ZIOHttp)).left.value.msg should
      include("Picked FutureEffect with ZIOHttp - Future effect will work only with Akka and Netty")
    FormValidator.validate(randomStarterRequest(FutureEffect, Http4s)).left.value.msg should
      include("Picked FutureEffect with Http4s - Future effect will work only with Akka and Netty")
    FormValidator.validate(randomStarterRequest(IOEffect, Akka)).left.value.msg should
      include("Picked IOEffect with Akka - IO effect will work only with Http4 and Netty")
    FormValidator.validate(randomStarterRequest(IOEffect, ZIOHttp)).left.value.msg should
      include("Picked IOEffect with ZIOHttp - IO effect will work only with Http4 and Netty")
    FormValidator.validate(randomStarterRequest(ZIOEffect, Akka)).left.value.msg should
      include("Picked ZIOEffect with Akka - ZIO effect will work only with Http4s and ZIOHttp")
    FormValidator.validate(randomStarterRequest(ZIOEffect, Netty)).left.value.msg should
      include("Picked ZIOEffect with Netty - ZIO effect will work only with Http4s and ZIOHttp")
  }

  it should "raise a problem when Akka implementation is requested with scala 3 project" in {
    FormValidator.validate(randomStarterRequest().copy(implementation = Akka, scalaVersion = Scala3)).left.value.msg should
      include("Scala3 version is not supported for Akka server implementation")
  }

  it should "not raise a problem with Effect and Implementation" in {
    val request = defaultRequest()
    val request1 = request.copy(effect = FutureEffect, implementation = Netty)
    val request2 = request.copy(effect = IOEffect, implementation = Netty)
    val request3 = request.copy(effect = IOEffect, implementation = Http4s)
    val request4 = request.copy(effect = ZIOEffect, implementation = Http4s)
    val request5 = request.copy(effect = ZIOEffect, implementation = ZIOHttp)

    FormValidator.validate(request).value shouldBe StarterDetails(
      request.projectName,
      request.groupId,
      ServerEffect.FutureEffect,
      ServerImplementation.Akka,
      addDocumentation = true,
      addMetrics = false,
      WithoutJson,
      Scala2,
      Builder.Sbt
    )
    FormValidator.validate(request1).value shouldBe StarterDetails(
      request1.projectName,
      request1.groupId,
      ServerEffect.FutureEffect,
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
      ServerEffect.IOEffect,
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
      ServerEffect.IOEffect,
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
      ServerEffect.ZIOEffect,
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
      ServerEffect.ZIOEffect,
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
      FutureEffect,
      Akka,
      addDocumentation = true,
      addMetrics = false,
      No,
      ScalaVersionRequest.Scala2,
      BuilderRequest.Sbt
    )
