package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}
import org.scalacheck.Gen

object StarterRequestGenerators {

  def randomStarterRequest(): StarterRequest = randomStarterRequestGen().sample.get
  def randomStarterRequest(effect: EffectRequest, implementation: ServerImplementationRequest): StarterRequest =
    randomStarterRequest().copy(effect = effect, implementation = implementation)

  private def randomStarterRequestGen(): Gen[StarterRequest] = {
    for {
      projectName <- Gen.alphaLowerStr
      groupId <- Gen.containerOf[List, String](Gen.alphaLowerStr).map(_.mkString("."))
      effect <- Gen.oneOf[EffectRequest](Seq(IOEffect, ZIOEffect, FutureEffect))
      serverImplementation <- Gen.oneOf[ServerImplementationRequest](Seq(Http4s, ZIOHttp, Akka, Netty))
    } yield StarterRequest(projectName, groupId, effect, implementation = serverImplementation)
  }
}
