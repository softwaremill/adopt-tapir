package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.*
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec.*
import sttp.tapir.Schema

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
) derives Decoder,
      Encoder.AsObject,
      Schema

/** Here I deviate from default circe marshallers to a specialized JsonTaggedAdt to serialize enums in a tags-constant only way. The reason
  * for the switch is that with circe-core 0.14 I was not able to customize the derivation in any elegant way, not to mention any match with
  * JsonTaggedAdt one liners. But if this improves with higher versions of circe, it would be advisable to fall back to circe in order to
  * reduce dependencies.
  */
enum EffectRequest(val toModel: ServerEffect, val legalServerImplementations: Set[ServerImplementation])
    derives JsonTaggedAdt.PureEncoder,
      JsonTaggedAdt.PureDecoder,
      Schema:
  case FutureEffect
      extends EffectRequest(
        ServerEffect.FutureEffect,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Pekko
        )
      )
  case IOEffect
      extends EffectRequest(
        ServerEffect.IOEffect,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Http4s
        )
      )
  case ZIOEffect
      extends EffectRequest(
        ServerEffect.ZIOEffect,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Http4s,
          ServerImplementation.ZIOHttp
        )
      )

enum ServerImplementationRequest(val toModel: ServerImplementation) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case Netty extends ServerImplementationRequest(ServerImplementation.Netty)
  case Http4s extends ServerImplementationRequest(ServerImplementation.Http4s)
  case ZIOHttp extends ServerImplementationRequest(ServerImplementation.ZIOHttp)
  case VertX extends ServerImplementationRequest(ServerImplementation.VertX)
  case Pekko extends ServerImplementationRequest(ServerImplementation.Pekko)

enum JsonImplementationRequest(val toModel: JsonImplementation) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case No extends JsonImplementationRequest(JsonImplementation.WithoutJson)
  case Circe extends JsonImplementationRequest(JsonImplementation.Circe)
  case Jsoniter extends JsonImplementationRequest(JsonImplementation.Jsoniter)
  case UPickle extends JsonImplementationRequest(JsonImplementation.UPickle)
  case ZIOJson extends JsonImplementationRequest(JsonImplementation.ZIOJson)
  case Pickler extends JsonImplementationRequest(JsonImplementation.Pickler)

enum ScalaVersionRequest(val toModel: ScalaVersion) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case Scala2 extends ScalaVersionRequest(ScalaVersion.Scala2)
  case Scala3 extends ScalaVersionRequest(ScalaVersion.Scala3)

enum BuilderRequest(val toModel: Builder) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case Sbt extends BuilderRequest(Builder.Sbt)
  case ScalaCli extends BuilderRequest(Builder.ScalaCli)
