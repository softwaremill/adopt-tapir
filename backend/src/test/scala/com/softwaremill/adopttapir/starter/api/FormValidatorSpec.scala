package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.ServerImplementation
import com.softwaremill.adopttapir.starter.StarterDetails.{FutureStarterDetails, IOStarterDetails, ZIOStarterDetails, defaultTapirVersion}
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZioEffect}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZioHttp}
import com.softwaremill.adopttapir.starter.api.StarterRequestGenerators.randomStarterRequest
import com.softwaremill.adopttapir.test.BaseTest

class FormValidatorSpec extends BaseTest {

  "FormValidator" should "raise a upper string in project name" in {
    // given
    val starterRequest = randomStarterRequest().copy(projectName = "UPPER project name")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include("Project name: `UPPER project name` should be written with lowercase")
  }

  it should "raise a whitespace in project name" in {
    // given
    val starterRequest = randomStarterRequest().copy(projectName = "UPPER project name")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include("Project name: `UPPER project name` should not contain whitespaces")
  }

  it should "raise a problem with groupId when it not follow Java naming package" in {
    // given
    val starterRequest = randomStarterRequest().copy(groupId = "Upper.Name.Package")

    // when
    val result = FormValidator.validate(starterRequest)

    // then
    result.left.value.msg should include("GroupId: `Upper.Name.Package` should follow Java package convention")
  }

  it should "raise a problem when effect will not match implementation" in {
    FormValidator.validate(randomStarterRequest(FutureEffect, ZioHttp)).left.value.msg should
      include("Picked FutureEffect with ZioHttp - Future effect will work only with Akka and Netty")
    FormValidator.validate(randomStarterRequest(FutureEffect, Http4s)).left.value.msg should
      include("Picked FutureEffect with Http4s - Future effect will work only with Akka and Netty")
    FormValidator.validate(randomStarterRequest(IOEffect, Akka)).left.value.msg should
      include("Picked IOEffect with Akka - IO effect will work only with Http4 and Netty")
    FormValidator.validate(randomStarterRequest(IOEffect, ZioHttp)).left.value.msg should
      include("Picked IOEffect with ZioHttp - IO effect will work only with Http4 and Netty")
    FormValidator.validate(randomStarterRequest(ZioEffect, Akka)).left.value.msg should
      include("Picked ZioEffect with Akka - ZIO effect will work only with Http4s and ZioHttp")
    FormValidator.validate(randomStarterRequest(ZioEffect, Netty)).left.value.msg should
      include("Picked ZioEffect with Netty - ZIO effect will work only with Http4s and ZioHttp")
  }

  it should "not raise a problem with Effect and Implementation" in {
    val request = StarterRequest("project", "com.softwaremill", FutureEffect, Akka)
    val request1 = request.copy(effect = FutureEffect, implementation = Netty)
    val request2 = request.copy(effect = IOEffect, implementation = Netty)
    val request3 = request.copy(effect = IOEffect, implementation = Http4s)
    val request4 = request.copy(effect = ZioEffect, implementation = Http4s)
    val request5 = request.copy(effect = ZioEffect, implementation = ZioHttp)

    FormValidator.validate(request).value shouldBe FutureStarterDetails(
      request.projectName,
      request.groupId,
      ServerImplementation.Akka,
      defaultTapirVersion
    )
    FormValidator.validate(request1).value shouldBe FutureStarterDetails(
      request1.projectName,
      request1.groupId,
      ServerImplementation.Netty,
      defaultTapirVersion
    )
    FormValidator.validate(request2).value shouldBe IOStarterDetails(
      request2.projectName,
      request2.groupId,
      ServerImplementation.Netty,
      defaultTapirVersion
    )
    FormValidator.validate(request3).value shouldBe IOStarterDetails(
      request3.projectName,
      request3.groupId,
      ServerImplementation.Http4s,
      defaultTapirVersion
    )
    FormValidator.validate(request4).value shouldBe ZIOStarterDetails(
      request4.projectName,
      request4.groupId,
      ServerImplementation.Http4s,
      defaultTapirVersion
    )
    FormValidator.validate(request5).value shouldBe ZIOStarterDetails(
      request5.projectName,
      request5.groupId,
      ServerImplementation.ZioHttp,
      defaultTapirVersion
    )
  }
}
