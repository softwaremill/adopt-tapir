package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.ServerImplementation

case class StarterRequest(projectName: String, groupId: String, effect: EffectRequest, implementation: ServerImplementationRequest)

sealed trait EffectRequest

object EffectRequest {
  case object IOEffect extends EffectRequest
  case object FutureEffect extends EffectRequest
  case object ZIOEffect extends EffectRequest
}

sealed trait ServerImplementationRequest {
  def toModel(): ServerImplementation
}

object ServerImplementationRequest {
  case object Akka extends ServerImplementationRequest {
    override def toModel(): ServerImplementation = ServerImplementation.Akka
  }
  case object Netty extends ServerImplementationRequest {
    override def toModel(): ServerImplementation = ServerImplementation.Netty
  }
  case object Http4s extends ServerImplementationRequest {
    override def toModel(): ServerImplementation = ServerImplementation.Http4s
  }
  case object ZIOHttp extends ServerImplementationRequest {
    override def toModel(): ServerImplementation = ServerImplementation.ZIOHttp
  }
}
