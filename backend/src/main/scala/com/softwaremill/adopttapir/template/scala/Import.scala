package com.softwaremill.adopttapir.template.scala

case class Import(fullName: String) {
  def asScalaImport(): String = s"import $fullName"
}
