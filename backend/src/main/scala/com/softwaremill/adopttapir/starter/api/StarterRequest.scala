package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.{Builder, JsonImplementation, ScalaVersion, ServerEffect, ServerImplementation}
import enumeratum.{CirceEnum, Enum, EnumEntry}

case class StarterRequest(
    projectName: String,
    groupId: String,
    effect: EffectRequest,
    implementation: ServerImplementationRequest,
    addDocumentation: Boolean,
    addMetrics: Boolean,
    json: JsonImplementationRequest,
    scalaVersion: ScalaVersionRequest,
    builder: BuilderRequest
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

/** Changes in JSON implementation have to be reflected in .github/workflows/adopt-tapir-ci.yml file so that running jobs in parallel is
  * still possible
  */
object JsonImplementationRequest extends Enum[JsonImplementationRequest] with CirceEnum[JsonImplementationRequest] {
  case object No extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.WithoutJson
  }

  case object Circe extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.Circe
  }

  case object UPickle extends JsonImplementationRequest {
    override def toModel: JsonImplementation = JsonImplementation.UPickle
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

/** Changes in Scala versions have to be reflected in .github/workflows/adopt-tapir-ci.yml file so that running jobs in parallel is still
  * possible
  */
object ScalaVersionRequest extends Enum[ScalaVersionRequest] with CirceEnum[ScalaVersionRequest] {
  case object Scala2 extends ScalaVersionRequest {
    override def toModel: ScalaVersion = ScalaVersion.Scala2
  }

  case object Scala3 extends ScalaVersionRequest {
    override def toModel: ScalaVersion = ScalaVersion.Scala3
  }

  override def values: IndexedSeq[ScalaVersionRequest] = findValues
}

sealed trait BuilderRequest extends EnumEntry {
  def toModel: Builder
}

object BuilderRequest extends Enum[BuilderRequest] with CirceEnum[BuilderRequest] {
  case object Sbt extends BuilderRequest {
    override def toModel: Builder = Builder.Sbt
  }

  case object ScalaCli extends BuilderRequest {
    override def toModel: Builder = Builder.ScalaCli
  }

  override def values: IndexedSeq[BuilderRequest] = findValues
}
