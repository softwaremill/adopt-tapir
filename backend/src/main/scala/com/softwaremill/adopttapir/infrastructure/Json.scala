package com.softwaremill.adopttapir.infrastructure

import com.softwaremill.adopttapir.util.Constants
import com.softwaremill.tagging.@@
import io.circe.{Decoder, Encoder, Printer}
import io.circe.generic.extras.{AutoDerivation, Configuration => CirceConfiguration}

/** Import the members of this object when doing JSON serialisation or deserialisation.
  */
object Json extends AutoDerivation {

  implicit val circeConfiguration: CirceConfiguration =
    CirceConfiguration.default
      .withDiscriminator(Constants.DiscriminatorName)
      .copy(transformConstructorNames = s => s.toLowerCase)

  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def taggedStringEncoder[U]: Encoder[String @@ U] = Encoder.encodeString.asInstanceOf[Encoder[String @@ U]]
  implicit def taggedStringDecoder[U]: Decoder[String @@ U] = Decoder.decodeString.asInstanceOf[Decoder[String @@ U]]
}
