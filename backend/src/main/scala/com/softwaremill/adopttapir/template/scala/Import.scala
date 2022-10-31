package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ScalaVersion

final case class Import(fullName: String):
  def asScalaImport(scalaVersion: ScalaVersion): String =
    val adjustedImport = scalaVersion match {
      case ScalaVersion.Scala2 => fullName
      case ScalaVersion.Scala3 => fullName.replace("._", ".*").replace(".{_", ".{*").replace("_}", "*}")
    }

    s"import $adjustedImport"
