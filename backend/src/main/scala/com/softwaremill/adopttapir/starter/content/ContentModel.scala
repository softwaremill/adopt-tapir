package com.softwaremill.adopttapir.starter.content

import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import sttp.tapir.Schema
import io.circe.*

sealed trait Node:
  def name: String

/** Here we diverged from using default deriving approach for both * Schema[Node] due to
  * https://tapir.softwaremill.com/en/latest/endpoint/schemas.html#derivation-for-recursive-types-in-scala3 * Encoder[Node] due to
  * limitations of circe 0.14 for Scala 3 wrt customizing serializers
  */
object Node:
  implicit def schema: Schema[Node] = Schema.derived[Node]

  given Encoder[Node] = Encoder.instance {
    case file @ File(_, _)     => file.asJson.mapObject(_.add("type", "file".asJson))
    case dir @ Directory(_, _) => dir.asJson.mapObject(_.add("type", "directory".asJson))
  }

  given Decoder[Node] = Decoder[File].or(Decoder[Directory].widen)

case class File(name: String, content: String) extends Node derives Codec.AsObject, Schema

case class Directory(name: String, content: List[Node]) extends Node derives Codec.AsObject, Schema:
  def childFiles(): List[File] = content.collect { case f: File => f }
  def childDirectories(): List[Directory] = content.collect { case d: Directory => d }
