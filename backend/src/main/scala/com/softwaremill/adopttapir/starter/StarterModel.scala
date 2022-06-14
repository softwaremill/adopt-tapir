package com.softwaremill.adopttapir.starter

case class StarterDetails(
    projectName: String,
    groupId: String,
    serverEffect: ServerEffect,
    serverImplementation: ServerImplementation,
    tapirVersion: String,
    addDocumentation: Boolean,
    jsonImplementation: JsonImplementation
)

object StarterDetails {
  val defaultTapirVersion = "1.0.0"
}

sealed trait ServerImplementation

object ServerImplementation {
  case object Akka extends ServerImplementation
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
