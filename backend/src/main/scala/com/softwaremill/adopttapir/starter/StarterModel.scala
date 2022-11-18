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

enum ServerImplementation:
  case Netty, Http4s, ZIOHttp, VertX

enum ServerEffect:
  case FutureEffect, IOEffect, ZIOEffect

enum JsonImplementation:
  case WithoutJson, Circe, UPickle, Jsoniter, ZIOJson

enum ScalaVersion(val value: String):
  case Scala2 extends ScalaVersion(TemplateDependencyInfo.scala2Version)
  case Scala3 extends ScalaVersion(TemplateDependencyInfo.scala3Version)

enum Builder:
  case Sbt, ScalaCli
