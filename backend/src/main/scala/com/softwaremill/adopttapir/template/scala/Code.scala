package com.softwaremill.adopttapir.template.scala

final case class Code(body: String, imports: Set[Import] = Set()):
  def addImport(`import`: Import): Code = copy(imports = this.imports + `import`)

  def addImports(imports: Set[Import]): Code = copy(imports = imports ++ this.imports)

  def prependBody(prependSection: String): Code = copy(body = prependSection ++ this.body)

object Code:
  val empty: Code = Code("")
