package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.{JsonImplementation, ScalaVersion, ServerEffect, ServerImplementation}
import enumeratum.{CirceEnum, Enum, EnumEntry}

case class StarterRequest(
    projectName: String,
    groupId: String,
    effect: EffectRequest,
    implementation: ServerImplementationRequest,
    addDocumentation: Boolean,
    addMetrics: Boolean,
    json: JsonImplementationRequest,
    scalaVersion: ScalaVersionRequest
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

sealed trait JsonImplementationRequest extends EnumEntry {
  def toModel: JsonImplementation
}

object JsonImplementationRequest extends Enum[JsonImplementationRequest] with CirceEnum[JsonImplementationRequest] {
  case object No extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.WithoutJson
  }

  case object Circe extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.Circe
  }

  case object Jsoniter extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.Jsoniter
  }

  case object ZIOJson extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.ZIOJson
  }

  override def values: IndexedSeq[JsonImplementationRequest] = findValues
}

sealed trait ScalaVersionRequest extends EnumEntry {
  def toModel: ScalaVersion
}

object ScalaVersionRequest extends Enum[ScalaVersionRequest] with CirceEnum[ScalaVersionRequest] {
  case object Scala2 extends ScalaVersionRequest {
    override def toModel: ScalaVersion = ScalaVersion.Scala2
  }

  case object Scala3 extends ScalaVersionRequest {
    override def toModel: ScalaVersion = ScalaVersion.Scala3
  }

  override def values: IndexedSeq[ScalaVersionRequest] = findValues
}
