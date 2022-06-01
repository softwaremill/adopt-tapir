package com.softwaremill.adopttapir.template.scala

case class PlainLogicWithImports(logic: String, additionalImports: List[Import] = Nil)

object PlainLogicWithImports {
  val empty = PlainLogicWithImports("")
}
