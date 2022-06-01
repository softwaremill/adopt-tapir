package com.softwaremill.adopttapir.template.scala

case class Code(body: String, imports: List[Import] = Nil)

object Code {
  val empty = Code("")
}
