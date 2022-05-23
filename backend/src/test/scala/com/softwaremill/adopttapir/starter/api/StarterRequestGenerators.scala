package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZioEffect}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZioHttp}
import org.scalacheck.Gen

object StarterRequestGenerators {

  def randomStarterRequest(): StarterRequest = randomStarterRequestGen().sample.get
  def randomStarterRequest(effect: EffectRequest, implementation: ServerImplementationRequest): StarterRequest =
    randomStarterRequest().copy(effect = effect, implementation = implementation)

  private def randomStarterRequestGen(): Gen[StarterRequest] = {
    for {
      projectName <- Gen.alphaLowerStr
      groupId <- Gen.containerOf[List, String](Gen.alphaLowerStr).map(_.mkString("."))
      effect <- Gen.oneOf[EffectRequest](Seq(IOEffect, ZioEffect, FutureEffect))
      serverImplementation <- Gen.oneOf[ServerImplementationRequest](Seq(Http4s, ZioHttp, Akka, Netty))
    } yield StarterRequest(projectName, groupId, effect, implementation = serverImplementation)
  }
}
