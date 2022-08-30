package com.softwaremill.adopttapir.infrastructure

import com.softwaremill.tagging.@@
import io.circe.{Decoder, Encoder, Printer}
import io.circe.generic.extras.{AutoDerivation, Configuration => CirceConfiguration}
import sttp.tapir.generic.{Configuration => TapirConfiguration}

/** Import the members of this object when doing JSON serialisation or deserialisation.
  */
object Json extends AutoDerivation {

  implicit lazy val circeConfiguration: CirceConfiguration =
    CirceConfiguration.default.withDiscriminator("type")
      .copy(transformConstructorNames = (s => s.toLowerCase)
    )

  implicit val tapirConfiguration: TapirConfiguration =
    TapirConfiguration.default.withDiscriminator("type")
      .copy(toDiscriminatorValue = _.fullName.split('.').last.stripSuffix("$").toLowerCase)

  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def taggedStringEncoder[U]: Encoder[String @@ U] = Encoder.encodeString.asInstanceOf[Encoder[String @@ U]]
  implicit def taggedStringDecoder[U]: Decoder[String @@ U] = Decoder.decodeString.asInstanceOf[Decoder[String @@ U]]
}
