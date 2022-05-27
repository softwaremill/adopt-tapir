package com.softwaremill.adopttapir.starter

sealed trait StarterDetails {
  val projectName: String
  val groupId: String
  val serverImplementation: ServerImplementation
  val tapirVersion: String
}

object StarterDetails {
  val defaultTapirVersion = "1.0.0-RC1"

  case class FutureStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation,
      tapirVersion: String
  ) extends StarterDetails

  case class IOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation,
      tapirVersion: String
  ) extends StarterDetails

  case class ZIOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation,
      tapirVersion: String
  ) extends StarterDetails

}

sealed trait ServerImplementation

object ServerImplementation {
  case object Akka extends ServerImplementation
  case object Netty extends ServerImplementation
  case object Http4s extends ServerImplementation
  case object ZIOHttp extends ServerImplementation
}
