package com.softwaremill.adopttapir.starter.api

import org.scalacheck.Gen
import com.softwaremill.adopttapir.starter.ScalaVersion

object StarterRequestGenerators:

  def randomStarterRequest(): StarterRequest = randomStarterRequestGen().sample.getOrElse(randomStarterRequest())

  def randomStarterRequest(stack: StackRequest, implementation: ServerImplementationRequest): StarterRequest =
    randomStarterRequest().copy(stack = stack, implementation = implementation, json = JsonImplementationRequest.Circe)

  private def randomStarterRequestGen(): Gen[StarterRequest] =
    for
      projectName <- Gen.alphaLowerStr suchThat (str => str == str.toLowerCase)
      groupId <- Gen.containerOf[List, String](Gen.alphaLowerStr suchThat (str => str.nonEmpty)).map(_.mkString(".")) suchThat (str =>
        str.length <= 256 && str.nonEmpty
      )
      scalaVersion <- Gen.oneOf(ScalaVersionRequest.values.toIndexedSeq)
      jsonValuesForScalaVersion = JsonImplementationRequest.values.toSet
      json <- Gen.oneOf(jsonValuesForScalaVersion.toIndexedSeq)
      effect <-
        if (json == JsonImplementationRequest.ZIOJson)
          Gen.const(StackRequest.ZIOStack)
        else
          Gen.oneOf(StackRequest.values.toIndexedSeq)
      serverImplementation <- Gen.oneOf(ServerImplementationRequest.values.toIndexedSeq)
      documentationAdded <- Gen.oneOf(true, false)
      metricsAdded <- Gen.oneOf(true, false)
      builder <- Gen.oneOf(BuilderRequest.values.toIndexedSeq)
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
