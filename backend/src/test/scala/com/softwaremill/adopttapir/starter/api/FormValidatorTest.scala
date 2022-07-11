package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.JsonImplementation.WithoutJson
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.No
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterRequestGenerators.randomStarterRequest
import com.softwaremill.adopttapir.starter.{ServerEffect, ServerImplementation, StarterDetails}
import com.softwaremill.adopttapir.test.BaseTest

class FormValidatorTest extends BaseTest {

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

  it should "raise  a problem with not valid semantic versioning notation for tapirVersion" in {
    // given
    val notValidTapirVersion = "1.0.0-alpha......1"
    val request = randomStarterRequest().copy(tapirVersion = notValidTapirVersion)

    // when
    val result = FormValidator.validate(request)

    // then
    result.left.value.msg should include(s"Provided input: `$notValidTapirVersion` is not in semantic versioning notation")
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
      request.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
    FormValidator.validate(request1).value shouldBe StarterDetails(
      request1.projectName,
      request1.groupId,
      ServerEffect.FutureEffect,
      ServerImplementation.Netty,
      request1.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
    FormValidator.validate(request2).value shouldBe StarterDetails(
      request2.projectName,
      request2.groupId,
      ServerEffect.IOEffect,
      ServerImplementation.Netty,
      request2.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
    FormValidator.validate(request3).value shouldBe StarterDetails(
      request3.projectName,
      request3.groupId,
      ServerEffect.IOEffect,
      ServerImplementation.Http4s,
      request3.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
    FormValidator.validate(request4).value shouldBe StarterDetails(
      request4.projectName,
      request4.groupId,
      ServerEffect.ZIOEffect,
      ServerImplementation.Http4s,
      request4.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
    FormValidator.validate(request5).value shouldBe StarterDetails(
      request5.projectName,
      request5.groupId,
      ServerEffect.ZIOEffect,
      ServerImplementation.ZIOHttp,
      request5.tapirVersion,
      addDocumentation = true,
      false,
      WithoutJson
    )
  }

  it should "not raise a problem with metrics for Future Akka implementation" in {
    val request = defaultRequest().copy(addMetrics = true)
    FormValidator.validate(request).value shouldBe StarterDetails(
      request.projectName,
      request.groupId,
      ServerEffect.FutureEffect,
      ServerImplementation.Akka,
      request.tapirVersion,
      addDocumentation = true,
      true,
      WithoutJson
    )
  }

  it should "raise a problem when metrics are enabled for anything but Future Akka implementation" in {
    val request = defaultRequest().copy(addMetrics = true)
    FormValidator.validate(request.copy(effect = FutureEffect, implementation = Netty)).left.value.msg should
      include("Picked FutureEffect with Netty - Metrics are supported for FutureEffect and Akka implementation only")
    FormValidator.validate(request.copy(effect = IOEffect, implementation = Netty)).left.value.msg should
      include("Picked IOEffect with Netty - Metrics are supported for FutureEffect and Akka implementation only")
    FormValidator.validate(request.copy(effect = IOEffect, implementation = Http4s)).left.value.msg should
      include("Picked IOEffect with Http4s - Metrics are supported for FutureEffect and Akka implementation only")
    FormValidator.validate(request.copy(effect = ZIOEffect, implementation = Http4s)).left.value.msg should
      include("Picked ZIOEffect with Http4s - Metrics are supported for FutureEffect and Akka implementation only")
    FormValidator.validate(request.copy(effect = ZIOEffect, implementation = ZIOHttp)).left.value.msg should
      include("Picked ZIOEffect with ZIOHttp - Metrics are supported for FutureEffect and Akka implementation only")
  }

  private def defaultRequest(): StarterRequest =
    StarterRequest("project", "com.softwaremill", FutureEffect, Akka, tapirVersion = "1.0.0", addDocumentation = true, false, No)
}
