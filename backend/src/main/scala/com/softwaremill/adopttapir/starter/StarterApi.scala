package com.softwaremill.adopttapir.starter

import cats.data.NonEmptyList
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.util.ServerEndpoints
import io.circe.generic.auto._
import sttp.tapir.generic.auto._

class StarterApi(http: Http, starterService: StarterService) {
  import http._

  private val starterPath = "starter.zip"

  private val starterEndpoint = baseEndpoint.get
    .in(starterPath)
    .in(jsonBody[StarterDetails])
    .out(fileBody)
    .serverLogic {
      starterService.generateZipFile(_).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        starterEndpoint
      )
      .map(_.tag(starterPath))
}

object StarterApi {

  object Queries {
    val tapirVersion = "tapirVersion"
    val scalaVersion = "scalaVersion"
    val sbtVersion = "sbtVersion"
    val group = "group"
    val artifact = "artifact"
    val projectName = "projectName"
    val description = "description"
    val packageName = "packageName"
    val dependencies = "dependencies"
  }
}
