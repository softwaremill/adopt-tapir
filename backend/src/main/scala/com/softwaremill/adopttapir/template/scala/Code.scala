package com.softwaremill.adopttapir.template.scala

case class Code(body: String, imports: Set[Import] = Set()) {
  def addImport(`import`: Import): Code = copy(imports = this.imports + `import`)

  def addImports(imports: Set[Import]): Code = copy(imports = imports ++ this.imports)
}

object Code {
  val empty = Code("")
}
