package com.softwaremill.adopttapir.infrastructure

import com.softwaremill.adopttapir.util.Constants
import com.softwaremill.tagging.@@
import io.circe.{Decoder, Encoder, Printer}

/** Import the members of this object when doing JSON serialisation or deserialisation.
  */
object Json:

  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
  given [U]: Encoder[String @@ U] = Encoder.encodeString.asInstanceOf[Encoder[String @@ U]]
  given[U]: Decoder[String @@ U] = Decoder.decodeString.asInstanceOf[Decoder[String @@ U]]
