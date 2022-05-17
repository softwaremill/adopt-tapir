package com.softwaremill.adopttapir.starter

import cats.data.NonEmptyList
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.starter.StarterApi.Queries._
import com.softwaremill.adopttapir.util.ServerEndpoints

class StarterApi(http: Http, starterService: StarterService) {
  import http._

  private val starterPath = "starter.zip"

  private val starterEndpoint = baseEndpoint.get
    .in(starterPath)
    .in(query[String](tapirVersion))
    .in(query[String](scalaVersion))
    .in(query[String](sbtVersion))
    .in(query[String](group))
    .in(query[String](artifact))
    .in(query[String](projectName))
    .in(query[String](packageName))
    .in(query[List[String]](dependencies))
    .out(fileBody)
    .serverLogic { case (tapirVersion, scalaVersion, sbtVersion, group, artifact, projectName, packageName, dependencies) =>
      val details = StarterDetails(
        tapirVersion,
        scalaVersion,
        sbtVersion,
        ProjectDetails(group, artifact, projectName, packageName),
        ModuleDependencies(dependencies)
      )

      starterService.generateZipFile(details).toOut
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
