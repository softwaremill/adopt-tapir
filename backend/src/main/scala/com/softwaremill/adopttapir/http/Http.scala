package com.softwaremill.adopttapir.http

import cats.effect.IO
import cats.implicits._
import com.softwaremill.adopttapir._
import com.softwaremill.adopttapir.infrastructure.Json._
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.tagging._
import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum
import sttp.tapir.generic.auto.SchemaDerivation
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.tapir.{Codec, Endpoint, EndpointOutput, PublicEndpoint, Schema, SchemaType, Tapir}
import sttp.tapir.generic.{Configuration => TapirConfiguration}

/** Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir. */
class Http() extends Tapir with TapirJsonCirce with TapirSchemas with TapirCodecEnumeratum with FLogging {

  implicit val tapirConfiguration: TapirConfiguration =
    TapirConfiguration.default
      .withDiscriminator(Http.DiscriminatorName)
      .copy(toDiscriminatorValue = TapirConfiguration.default.toEncodedName.compose(_.fullName.toLowerCase))

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

  implicit class IOOut[T](f: IO[T]) {

    /** An extension method for [[IO]], which converts a possibly failed IO, to one which either returns the error converted to an
      * [[Error_OUT]] instance, or returns the successful value unchanged.
      */
    def toOut: IO[Either[(StatusCode, Error_OUT), T]] = {
      f.map(t => t.asRight[(StatusCode, Error_OUT)]).recoverWith { case f: Fail =>
        val (statusCode, message) = failToResponseData(f)
        logger.warn[IO](s"Request fail: ${message.error}").map(_ => (statusCode, message).asLeft[T])
      }
    }
  }

  override def jsonPrinter: Printer = noNullsPrinter
}

object Http {
  val DiscriminatorName = "type"
}

/** Schemas for types used in endpoint descriptions (as parts of query parameters, JSON bodies, etc.). Includes explicitly defined schemas
  * for custom types, and auto-derivation for ADTs & value classes.
  */
trait TapirSchemas extends SchemaDerivation {
  implicit def taggedPlainCodec[U, T](implicit uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  implicit def schemaForTagged[U, T](implicit uc: Schema[U]): Schema[U @@ T] = uc.asInstanceOf[Schema[U @@ T]]
}

case class Error_OUT(error: String)
