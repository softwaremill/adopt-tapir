package com.softwaremill.adopttapir.starter

import com.softwaremill.adopttapir.version.TemplateDependencyInfo

/** In case of modifying [[StarterDetails.projectName]] or [[StarterDetails.groupId]] update also
  * [[com.softwaremill.adopttapir.metrics.Metrics.excludedStarterDetailsFields]]
  */
case class StarterDetails(
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

sealed trait ServerImplementation

object ServerImplementation {
  case object Netty extends ServerImplementation

  case object Http4s extends ServerImplementation

  case object ZIOHttp extends ServerImplementation
}

sealed trait ServerEffect

object ServerEffect {
  case object FutureEffect extends ServerEffect

  case object IOEffect extends ServerEffect

  case object ZIOEffect extends ServerEffect
}

sealed trait JsonImplementation

object JsonImplementation {
  case object WithoutJson extends JsonImplementation

  case object Circe extends JsonImplementation

  case object Jsoniter extends JsonImplementation

  case object ZIOJson extends JsonImplementation
}

sealed trait ScalaVersion {
  val value: String
}

object ScalaVersion {
  case object Scala2 extends ScalaVersion {
    override val value: String = TemplateDependencyInfo.scala2Version
  }

  case object Scala3 extends ScalaVersion {
    override val value: String = TemplateDependencyInfo.scala3Version
  }
}

sealed trait Builder

object Builder {
  case object Sbt extends Builder

  case object ScalaCli extends Builder
}
