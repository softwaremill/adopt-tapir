package com.softwaremill.adopttapir.starter

sealed trait StarterDetails {
  val projectName: String
  val groupId: String
  val serverImplementation: ServerImplementation
}

object StarterDetails {

  case class FutureStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation
  ) extends StarterDetails

  case class IOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation
  ) extends StarterDetails

  case class ZIOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation
  ) extends StarterDetails

}

sealed trait ServerImplementation

object ServerImplementation {
  case object Akka extends ServerImplementation
  case object Netty extends ServerImplementation
  case object Http4s extends ServerImplementation
  case object ZioHttp extends ServerImplementation
}
