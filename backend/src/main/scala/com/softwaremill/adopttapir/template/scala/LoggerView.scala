package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.*
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterDetails}

object LoggerView:

  def getProperLoggerContent(starterDetails: StarterDetails): String =
    starterDetails.scalaVersion match {
      case ScalaVersion.Scala2 => txt.LoggerZIOhttpZIO(starterDetails.groupId).toString()
      case ScalaVersion.Scala3 => txt.LoggerZIOhttpZIOScala3(starterDetails.groupId).toString()
    }
