package com.softwaremill.adopttapir.metrics

import cats.effect.IO
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.infrastructure.Json.*
import com.softwaremill.adopttapir.version.BuildInfo
import sttp.tapir.server.ServerEndpoint
import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.*

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
  final case class Version_OUT(buildSha: String) derives Decoder

  object Version_OUT:
    inline given Encoder[Version_OUT] = deriveEncoder
