package com.softwaremill.adopttapir.metrics

import cats.effect.IO
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.infrastructure.Json.*
import com.softwaremill.adopttapir.version.BuildInfo
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information.
  */
class VersionApi(http: Http):
  import VersionApi.*
  import http.*

  val versionEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      IO(Version_OUT(BuildInfo.lastCommitHash)).toOut
    }

object VersionApi:
  final case class Version_OUT(buildSha: String) derives Decoder, Encoder.AsObject, Schema
