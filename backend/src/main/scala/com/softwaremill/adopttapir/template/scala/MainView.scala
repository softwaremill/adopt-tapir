package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation._
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterDetails}

object MainView {

  def getProperMainContent(starterDetails: StarterDetails): String = {
    starterDetails.scalaVersion match {
      case ScalaVersion.Scala2 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureEffect, Akka, addDocumentation, addMetrics, _, _) =>
            txt.MainFutureAkka(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, FutureEffect, Netty, addDocumentation, false, _, _) =>
            txt.MainFutureNetty(groupId, addDocumentation).toString()
          case StarterDetails(_, groupId, IOEffect, Http4s, addDocumentation, addMetrics, _, _) =>
            txt.MainIOHttp4s(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOEffect, Netty, addDocumentation, false, _, _) =>
            txt.MainIONetty(groupId, addDocumentation).toString()
          case StarterDetails(_, groupId, ZIOEffect, Http4s, addDocumentation, addMetrics, _, _) =>
            txt.MainZIOHttp4s(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, addDocumentation, addMetrics, _, _) =>
            txt.MainZIOhttpZIO(groupId, addDocumentation, addMetrics).toString()
          case _ => throw new UnsupportedOperationException(s"$starterDetails not supported")
        }
      case ScalaVersion.Scala3 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureEffect, Netty, addDocumentation, false, _, _) =>
            txt.MainFutureNettyScala3(groupId, addDocumentation).toString()
          case StarterDetails(_, groupId, IOEffect, Http4s, addDocumentation, addMetrics, _, _) =>
            txt.MainIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOEffect, Netty, addDocumentation, false, _, _) =>
            txt.MainIONettyScala3(groupId, addDocumentation).toString()
          case StarterDetails(_, groupId, ZIOEffect, Http4s, addDocumentation, addMetrics, _, _) =>
            txt.MainZIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, addDocumentation, addMetrics, _, _) =>
            txt.MainZIOhttpZIOScala3(groupId, addDocumentation, addMetrics).toString()
          case _ => throw new UnsupportedOperationException(s"$starterDetails not supported")
        }

    }

  }
}
