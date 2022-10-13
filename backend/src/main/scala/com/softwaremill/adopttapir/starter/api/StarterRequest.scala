package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.{Builder, JsonImplementation, ScalaVersion, ServerEffect, ServerImplementation}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import org.latestbit.circe.adt.codec.*

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
) derives Decoder

object StarterRequest:
  inline given Encoder[StarterRequest] = deriveEncoder


enum EffectRequest(val toModel: ServerEffect) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
  case FutureEffect extends EffectRequest(ServerEffect.FutureEffect)
  case IOEffect extends EffectRequest(ServerEffect.IOEffect)
  case ZIOEffect extends EffectRequest(ServerEffect.ZIOEffect)

enum ServerImplementationRequest(val toModel: ServerImplementation) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
  case Netty extends ServerImplementationRequest(ServerImplementation.Netty)
  case Http4s extends ServerImplementationRequest(ServerImplementation.Http4s)
  case ZIOHttp extends ServerImplementationRequest(ServerImplementation.ZIOHttp)

enum JsonImplementationRequest(val toModel: JsonImplementation) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
  case No extends JsonImplementationRequest(JsonImplementation.WithoutJson)
  case Circe extends JsonImplementationRequest(JsonImplementation.Circe)
  case Jsoniter extends JsonImplementationRequest(JsonImplementation.Jsoniter)
  case UPickle extends JsonImplementationRequest(JsonImplementation.UPickle)
  case ZIOJson extends JsonImplementationRequest(JsonImplementation.ZIOJson)

enum ScalaVersionRequest(val toModel: ScalaVersion) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
  case Scala2 extends ScalaVersionRequest(ScalaVersion.Scala2)
  case Scala3 extends ScalaVersionRequest(ScalaVersion.Scala3)

enum  BuilderRequest(val toModel: Builder) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
  case Sbt extends BuilderRequest(Builder.Sbt)
  case ScalaCli extends BuilderRequest(Builder.ScalaCli)
