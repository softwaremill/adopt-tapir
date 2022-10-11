package com.softwaremill.adopttapir.starter.content

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.*
import sttp.tapir.Schema
import io.circe.*

sealed trait Node derives Codec.AsObject:
  def name: String

object Node:
  implicit def schema: Schema[Node] = Schema.derived[Node]

case class File(name: String, content: String) extends Node derives Decoder, Encoder.AsObject

case class Directory(name: String, content: List[Node]) extends Node derives Decoder, Encoder.AsObject:
  def childFiles(): List[File] = content.collect { case f: File => f }
  def childDirectories(): List[Directory] = content.collect { case d: Directory => d }
