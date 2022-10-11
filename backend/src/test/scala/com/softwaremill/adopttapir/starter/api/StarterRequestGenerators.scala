package com.softwaremill.adopttapir.starter.api

import org.scalacheck.Gen

object StarterRequestGenerators {

  def randomStarterRequest(): StarterRequest = randomStarterRequestGen().sample.getOrElse(randomStarterRequest())

  def randomStarterRequest(effect: EffectRequest, implementation: ServerImplementationRequest): StarterRequest =
    randomStarterRequest().copy(effect = effect, implementation = implementation)

  private def randomStarterRequestGen(): Gen[StarterRequest] = {
    for
      projectName <- Gen.alphaLowerStr suchThat (str => str == str.toLowerCase)
      groupId <- Gen.containerOf[List, String](Gen.alphaLowerStr suchThat (str => str.nonEmpty)).map(_.mkString(".")) suchThat (str =>
        str.length <= 256 && str.nonEmpty
      )
      effect <- Gen.oneOf(EffectRequest.values)
      serverImplementation <- Gen.oneOf(ServerImplementationRequest.values)
      documentationAdded <- Gen.oneOf(true, false)
      metricsAdded <- Gen.oneOf(true, false)
      json <- Gen.oneOf(JsonImplementationRequest.values)
      scalaVersion <- Gen.oneOf(ScalaVersionRequest.values)
      builder <- Gen.oneOf(BuilderRequest.values)
    yield StarterRequest(
      projectName,
      groupId,
      effect,
      implementation = serverImplementation,
      documentationAdded,
      metricsAdded,
      json,
      scalaVersion,
      builder
    )
  }
}
