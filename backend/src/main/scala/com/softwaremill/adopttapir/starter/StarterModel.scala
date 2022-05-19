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
      serverImplementation: ServerImplementation with FutureEff,
  ) extends StarterDetails

  case class IOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation with IOEff,
  ) extends StarterDetails

  case class ZIOStarterDetails(
      projectName: String,
      groupId: String,
      serverImplementation: ServerImplementation with ZIOEff,
  ) extends StarterDetails

}

sealed trait ServerImplementation

object ServerImplementation {
  case object Akka extends ServerImplementation with FutureEff
  case object Netty extends ServerImplementation with FutureEff with IOEff
  case object Http4s extends ServerImplementation with IOEff with ZIOEff
  case object ZioHttp extends ServerImplementation with ZIOEff
}

sealed trait EffectType
trait FutureEff extends EffectType
trait IOEff extends EffectType
trait ZIOEff extends EffectType
