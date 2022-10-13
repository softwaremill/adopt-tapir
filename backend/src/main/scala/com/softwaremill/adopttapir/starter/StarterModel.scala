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

//sealed trait ServerImplementation
//
//object ServerImplementation {
//  case object Akka extends ServerImplementation
//
//  case object Netty extends ServerImplementation
//
//  case object Http4s extends ServerImplementation
//
//  case object ZIOHttp extends ServerImplementation
//}

enum ServerImplementation:
  case Netty, Http4s, ZIOHttp

//sealed trait ServerEffect
//
//object ServerEffect {
//  case object FutureEffect extends ServerEffect
//
//  case object IOEffect extends ServerEffect
//
//  case object ZIOEffect extends ServerEffect
//}

enum ServerEffect:
  case FutureEffect, IOEffect, ZIOEffect

//sealed trait JsonImplementation
//
//object JsonImplementation {
//  case object WithoutJson extends JsonImplementation
//
//  case object Circe extends JsonImplementation
//
//  case object Jsoniter extends JsonImplementation
//
//  case object ZIOJson extends JsonImplementation
//}

enum JsonImplementation:
  case WithoutJson, Circe, UPickle, Jsoniter, ZIOJson

//sealed trait ScalaVersion {
//  val value: String
//}

//object ScalaVersion {
//  case object Scala2 extends ScalaVersion {
//    override val value: String = TemplateDependencyInfo.scala2Version
//  }
//
//  case object Scala3 extends ScalaVersion {
//    override val value: String = TemplateDependencyInfo.scala3Version
//  }
//}

enum ScalaVersion(val value: String):
  case Scala2 extends ScalaVersion(TemplateDependencyInfo.scala2Version)
  case Scala3 extends ScalaVersion(TemplateDependencyInfo.scala3Version)

//sealed trait Builder

//object Builder {
//  case object Sbt extends Builder
//
//  case object ScalaCli extends Builder
//}

enum Builder:
  case Sbt, ScalaCli
