package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}
import org.scalacheck.Gen

object StarterRequestGenerators {

  def randomStarterRequest(): StarterRequest = randomStarterRequestGen().sample.getOrElse(randomStarterRequest())

  def randomStarterRequest(effect: EffectRequest, implementation: ServerImplementationRequest): StarterRequest =
    randomStarterRequest().copy(effect = effect, implementation = implementation)

  private def randomStarterRequestGen(): Gen[StarterRequest] = {
    for {
      projectName <- Gen.alphaLowerStr suchThat (str => str == str.toLowerCase)
      groupId <- Gen.containerOf[List, String](Gen.alphaLowerStr).map(_.mkString(".")) suchThat (str => str.length <= 256 && str.nonEmpty)
      effect <- Gen.oneOf[EffectRequest](Seq(IOEffect, ZIOEffect, FutureEffect))
      serverImplementation <- Gen.oneOf[ServerImplementationRequest](Seq(Http4s, ZIOHttp, Akka, Netty))
      documentationAdded <- Gen.oneOf(true, false)
    } yield StarterRequest(
      projectName,
      groupId,
      effect,
      implementation = serverImplementation,
      StarterDetails.defaultTapirVersion,
      documentationAdded
    )
  }
}
