package com.softwaremill.adopttapir.starter

import com.softwaremill.adopttapir.version.TemplateDependencyInfo

/** In case of modifying [[StarterDetails.projectName]] or [[StarterDetails.groupId]] update also
  * [[com.softwaremill.adopttapir.metrics.Metrics.excludedStarterDetailsFields]]
  */

final case class StarterDetails(
    projectName: String,
    groupId: String,
    serverEffect: ServerEffect,
    serverImplementation: ServerImplementation,
    addDocumentation: Boolean,
    addMetrics: Boolean,
    jsonImplementation: JsonImplementation,
    scalaVersion: ScalaVersion,
    builder: Builder
)

object ServerEffectAndImplementation {
  def unapply(starterDetails: StarterDetails): (ServerEffect, ServerImplementation) =
    starterDetails match {
      case StarterDetails(_, _, serverEffect, serverImplementation, _, _, _, _, _) =>
        (serverEffect, serverImplementation)
    }
}

enum ServerImplementation(val name: String):
  case Netty extends ServerImplementation("Netty")
  case Http4s extends ServerImplementation("Http4s")
  case ZIOHttp extends ServerImplementation("ZIO Http")
  case VertX extends ServerImplementation("Vert.X")

enum ServerEffect(val name: String):
  case FutureEffect extends ServerEffect("Future")
  case IOEffect extends ServerEffect("IO")
  case ZIOEffect extends ServerEffect("ZIO")

enum JsonImplementation:
  case WithoutJson, Circe, UPickle, Jsoniter, ZIOJson

enum ScalaVersion(val value: String):
  case Scala2 extends ScalaVersion(TemplateDependencyInfo.scala2Version)
  case Scala3 extends ScalaVersion(TemplateDependencyInfo.scala3Version)

enum Builder:
  case Sbt, ScalaCli
