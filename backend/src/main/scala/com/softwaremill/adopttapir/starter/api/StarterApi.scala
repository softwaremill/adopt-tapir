package com.softwaremill.adopttapir.starter.api

import cats.data.NonEmptyList
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.starter.StarterDetails.{FutureStarterDetails, IOStarterDetails, ZIOStarterDetails}
import com.softwaremill.adopttapir.starter._
import com.softwaremill.adopttapir.util.ServerEndpoints
import io.circe.generic.auto._
import sttp.tapir.generic.auto._

class StarterApi(http: Http, starterService: StarterService) {
  import http._

  private val starterPath = "starter.zip"

  private val starterEndpoint = baseEndpoint.get
    .in(starterPath)
    .in(jsonBody[StarterRequest])
    .out(fileBody)
    .serverLogic { request =>
      val details = transform(request)
      starterService.generateZipFile(details).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        starterEndpoint
      )
      .map(_.tag(starterPath))

  // TODO: add validation for all properties
  private def transform(r: StarterRequest) = {
    r.effect match {
      case Effect.FutureEffect =>
        FutureStarterDetails(r.projectName, r.groupId, r.serverImplementation.asInstanceOf[FutureEff with ServerImplementation])
      case Effect.IOEffect =>
        IOStarterDetails(r.projectName, r.groupId, r.serverImplementation.asInstanceOf[IOEff with ServerImplementation])
      case Effect.ZioEffect =>
        ZIOStarterDetails(r.projectName, r.groupId, r.serverImplementation.asInstanceOf[ZIOEff with ServerImplementation])
    }
  }

}
