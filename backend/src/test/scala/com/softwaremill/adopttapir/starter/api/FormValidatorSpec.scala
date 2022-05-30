package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.ServerImplementation
import com.softwaremill.adopttapir.starter.StarterDetails.{FutureStarterDetails, IOStarterDetails, ZIOStarterDetails, defaultTapirVersion}
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterRequestGenerators.randomStarterRequest
import com.softwaremill.adopttapir.test.BaseTest

class FormValidatorSpec extends BaseTest {

  "FormValidator" should "raise a problem with project name" in {
    // given
    val starterRequest = randomStarterRequest().copy(projectName = "UPPER project name")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include(s"Project name: `${starterRequest.projectName}` should match regex: `[a-z0-9]+`")
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
    val request = StarterRequest("project", "com.softwaremill", FutureEffect, Akka, tapirVersion = "1.0.0-RC1")
    val request1 = request.copy(effect = FutureEffect, implementation = Netty)
    val request2 = request.copy(effect = IOEffect, implementation = Netty)
    val request3 = request.copy(effect = IOEffect, implementation = Http4s)
    val request4 = request.copy(effect = ZIOEffect, implementation = Http4s)
    val request5 = request.copy(effect = ZIOEffect, implementation = ZIOHttp)

    FormValidator.validate(request).value shouldBe FutureStarterDetails(
      request.projectName,
      request.groupId,
      ServerImplementation.Akka,
      request.tapirVersion
    )
    FormValidator.validate(request1).value shouldBe FutureStarterDetails(
      request1.projectName,
      request1.groupId,
      ServerImplementation.Netty,
      request1.tapirVersion
    )
    FormValidator.validate(request2).value shouldBe IOStarterDetails(
      request2.projectName,
      request2.groupId,
      ServerImplementation.Netty,
      request2.tapirVersion
    )
    FormValidator.validate(request3).value shouldBe IOStarterDetails(
      request3.projectName,
      request3.groupId,
      ServerImplementation.Http4s,
      request3.tapirVersion
    )
    FormValidator.validate(request4).value shouldBe ZIOStarterDetails(
      request4.projectName,
      request4.groupId,
      ServerImplementation.Http4s,
      request4.tapirVersion
    )
    FormValidator.validate(request5).value shouldBe ZIOStarterDetails(
      request5.projectName,
      request5.groupId,
      ServerImplementation.ZIOHttp,
      request5.tapirVersion
    )
  }
}
