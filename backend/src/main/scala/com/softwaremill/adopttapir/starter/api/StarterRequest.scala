package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.*
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec.*
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.description

case class StarterRequest(
    @description("The name of the generated project")
    projectName: String,
    @description("The group id for the generated project, e.g. com.softwaremill")
    groupId: String,
    @description(
      """The effect stack for the generated project. Each stack supports a specific set of server implementations:
        |<ul>
        |  <li><b>FutureStack</b>: Netty, VertX, Pekko</li>
        |  <li><b>IOStack</b>: Netty, VertX, Http4s</li>
        |  <li><b>ZIOStack</b>: Netty, VertX, Http4s, ZIOHttp</li>
        |  <li><b>OxStack</b>: Netty</li>
        |</ul>""".stripMargin
    )
    stack: StackRequest,
    @description(
      """The HTTP server implementation to use. Must be compatible with the chosen <b>stack</b>:
        |<ul>
        |  <li><b>Netty</b></li>
        |  <li><b>Http4s</b></li>
        |  <li><b>ZIOHttp</b></li>
        |  <li><b>VertX</b></li>
        |  <li><b>Pekko</b></li>
        |</ul>""".stripMargin
    )
    implementation: ServerImplementationRequest,
    @description("If <b>true</b>, generates OpenAPI documentation with Swagger UI for the endpoints")
    addDocumentation: Boolean,
    @description("If <b>true</b>, wires in OpenTelemetry metrics for the endpoints with auto configured exporter")
    addMetrics: Boolean,
    @description(
      """The JSON library used for request/response body serialization:
        |<ul>
        |  <li><b>No</b>: no JSON support</li>
        |  <li><b>Circe</b></li>
        |  <li><b>Jsoniter</b></li>
        |  <li><b>UPickle</b></li>
        |  <li><b>ZIOJson</b>: ZIOStack only</li>
        |</ul>""".stripMargin
    )
    json: JsonImplementationRequest,
    @description(
      """The Scala version for the generated project:
        |<ul>
        |  <li><b>Scala2</b></li>
        |  <li><b>Scala3</b></li>
        |</ul>""".stripMargin
    )
    scalaVersion: ScalaVersionRequest,
    @description(
      """The build tool for the generated project:
        |<ul>
        |  <li><b>Sbt</b>: sbt build definition, generates a multi file project with tests</li>
        |  <li><b>ScalaCli</b>: scala-cli, generates single-file build, without tests</li>
        |</ul>""".stripMargin
    )
    builder: BuilderRequest
) derives Decoder,
      Encoder.AsObject,
      Schema

/** Here I deviate from default circe marshallers to a specialized JsonTaggedAdt to serialize enums in a tags-constant only way. The reason
  * for the switch is that with circe-core 0.14 I was not able to customize the derivation in any elegant way, not to mention any match with
  * JsonTaggedAdt one liners. But if this improves with higher versions of circe, it would be advisable to fall back to circe in order to
  * reduce dependencies.
  */
enum StackRequest(val toModel: ServerStack, val legalServerImplementations: Set[ServerImplementation])
    derives JsonTaggedAdt.PureEncoder,
      JsonTaggedAdt.PureDecoder,
      Schema:
  case FutureStack
      extends StackRequest(
        ServerStack.FutureStack,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Pekko
        )
      )
  case IOStack
      extends StackRequest(
        ServerStack.IOStack,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Http4s
        )
      )
  case ZIOStack
      extends StackRequest(
        ServerStack.ZIOStack,
        legalServerImplementations = Set(
          ServerImplementation.Netty,
          ServerImplementation.VertX,
          ServerImplementation.Http4s,
          ServerImplementation.ZIOHttp
        )
      )
  case OxStack
      extends StackRequest(
        ServerStack.OxStack,
        legalServerImplementations = Set(
          ServerImplementation.Netty
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

enum ScalaVersionRequest(val toModel: ScalaVersion) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case Scala2 extends ScalaVersionRequest(ScalaVersion.Scala2)
  case Scala3 extends ScalaVersionRequest(ScalaVersion.Scala3)

enum BuilderRequest(val toModel: Builder) derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder, Schema:
  case Sbt extends BuilderRequest(Builder.Sbt)
  case ScalaCli extends BuilderRequest(Builder.ScalaCli)
