package com.softwaremill.adopttapir.http

import cats.effect.IO
import cats.implicits.*
import com.softwaremill.adopttapir.*
import com.softwaremill.adopttapir.infrastructure.Json.*
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.util.Constants
import com.softwaremill.tagging.*
import io.circe.{Printer, Encoder, Decoder}
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.tapir.{Codec, Endpoint, EndpointOutput, PublicEndpoint, Schema, SchemaType, Tapir}
import sttp.tapir.generic.Configuration as TapirConfiguration
import io.circe.generic.semiauto.*

/** Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir. */
class Http() extends Tapir, TapirJsonCirce, FLogging:

  given TapirConfiguration =
    TapirConfiguration.default
      .withDiscriminator(Constants.DiscriminatorName)
      .copy(toDiscriminatorValue = TapirConfiguration.default.toDiscriminatorValue.andThen(_.toLowerCase))

  val jsonErrorOutOutput: EndpointOutput[Error_OUT] = jsonBody[Error_OUT]

  /** Description of the output, that is used to represent an error that occurred during endpoint invocation. */
  val failOutput: EndpointOutput[(StatusCode, Error_OUT)] = statusCode.and(jsonErrorOutOutput)

  /** Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class.
    */
  val baseEndpoint: PublicEndpoint[Unit, (StatusCode, Error_OUT), Unit, Any] =
    endpoint.errorOut(failOutput)

  val failToResponseData: Fail => (StatusCode, Error_OUT) = {
    case Fail.NotFound(what)      => (StatusCode.NotFound, Error_OUT(what))
    case Fail.Conflict(msg)       => (StatusCode.Conflict, Error_OUT(msg))
    case Fail.IncorrectInput(msg) => (StatusCode.BadRequest, Error_OUT(msg))
    case Fail.Forbidden           => (StatusCode.Forbidden, Error_OUT("Forbidden"))
    case Fail.Unauthorized(msg)   => (StatusCode.Unauthorized, Error_OUT(msg))
    case _                        => (StatusCode.InternalServerError, Error_OUT("Internal server error"))
  }

  extension [T](io: IO[T])
    def toOut: IO[Either[(StatusCode, Error_OUT), T]] =
      io.map(t => t.asRight[(StatusCode, Error_OUT)]).recoverWith { case f: Fail =>
        val (statusCode, message) = failToResponseData(f)
        logger.warn[IO](s"Request fail: ${message.error}").map(_ => (statusCode, message).asLeft[T])
      }

  override def jsonPrinter: Printer = noNullsPrinter

end Http

final case class Error_OUT(error: String) derives Decoder, Encoder.AsObject, Schema
