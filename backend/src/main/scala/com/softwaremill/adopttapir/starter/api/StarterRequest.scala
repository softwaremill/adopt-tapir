package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.{ServerEffect, ServerImplementation}
import enumeratum.{CirceEnum, Enum, EnumEntry}

case class StarterRequest(
    projectName: String,
    groupId: String,
    effect: EffectRequest,
    implementation: ServerImplementationRequest,
    tapirVersion: String
)

object StarterRequest {}

sealed trait EffectRequest extends EnumEntry {
  def toModel: ServerEffect
}

object EffectRequest extends Enum[EffectRequest] with CirceEnum[EffectRequest] {

  case object FutureEffect extends EffectRequest {
    override def toModel: ServerEffect = ServerEffect.FutureEffect
  }
  case object IOEffect extends EffectRequest {
    override def toModel: ServerEffect = ServerEffect.IOEffect
  }
  case object ZIOEffect extends EffectRequest {
    override def toModel: ServerEffect = ServerEffect.ZIOEffect
  }

  override def values: IndexedSeq[EffectRequest] = findValues
}

sealed trait ServerImplementationRequest extends EnumEntry {
  def toModel: ServerImplementation
}

object ServerImplementationRequest extends Enum[ServerImplementationRequest] with CirceEnum[ServerImplementationRequest] {
  case object Akka extends ServerImplementationRequest {
    override def toModel: ServerImplementation = ServerImplementation.Akka
  }
  case object Netty extends ServerImplementationRequest {
    override def toModel: ServerImplementation = ServerImplementation.Netty
  }
  case object Http4s extends ServerImplementationRequest {
    override def toModel: ServerImplementation = ServerImplementation.Http4s
  }
  case object ZIOHttp extends ServerImplementationRequest {
    override def toModel: ServerImplementation = ServerImplementation.ZIOHttp
  }
  override def values: IndexedSeq[ServerImplementationRequest] = findValues
}
