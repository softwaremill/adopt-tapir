package com.softwaremill.adopttapir.starter

import com.softwaremill.adopttapir.version.TemplateDependencyInfo

/** In case of modifying [[StarterDetails.projectName]] or [[StarterDetails.groupId]] update also
  * [[com.softwaremill.adopttapir.metrics.Metrics.excludedStarterDetailsFields]]
  */

final case class StarterDetails(
    projectName: String,
    groupId: String,
    serverStack: ServerStack,
    serverImplementation: ServerImplementation,
    addDocumentation: Boolean,
    addMetrics: Boolean,
    jsonImplementation: JsonImplementation,
    scalaVersion: ScalaVersion,
    builder: Builder
)

object ServerStackAndImplementation {
  def unapply(starterDetails: StarterDetails): (ServerStack, ServerImplementation) =
    starterDetails match {
      case StarterDetails(_, _, serverStack, serverImplementation, _, _, _, _, _) =>
        (serverStack, serverImplementation)
    }
}

enum ServerImplementation(val name: String):
  case Netty extends ServerImplementation("Netty")
  case Http4s extends ServerImplementation("Http4s")
  case ZIOHttp extends ServerImplementation("ZIO Http")
  case VertX extends ServerImplementation("Vert.X")
  case Pekko extends ServerImplementation("Pekko")

enum ServerStack(val name: String):
  case FutureStack extends ServerStack("Future")
  case IOStack extends ServerStack("IO")
  case ZIOStack extends ServerStack("ZIO")
  case OxStack extends ServerStack("Ox")

enum JsonImplementation:
  case WithoutJson, Circe, UPickle, Jsoniter, ZIOJson

enum ScalaVersion(val value: String):
  case Scala2 extends ScalaVersion(TemplateDependencyInfo.scala2Version)
  case Scala3 extends ScalaVersion(TemplateDependencyInfo.scala3Version)

enum Builder:
  case Sbt, ScalaCli
