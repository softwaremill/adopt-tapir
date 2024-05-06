package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerStack.{FutureStack, IOStack, OxStack, ZIOStack}
import com.softwaremill.adopttapir.starter.ServerImplementation.*
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterDetails}

object MainView:

  def getProperMainContent(starterDetails: StarterDetails): String =
    starterDetails.scalaVersion match {
      case ScalaVersion.Scala2 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureNetty(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, FutureStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureVertx(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, FutureStack, Pekko, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFuturePekko(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOHttp4s(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIONetty(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOVertx(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOHttp4s(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIONetty(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, ZIOHttp, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOhttpZIO(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOVertx(groupId, addDocumentation, addMetrics).toString()
          case _ => throw UnsupportedOperationException(s"$starterDetails not supported")
        }
      case ScalaVersion.Scala3 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureNettyScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, FutureStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureVertxScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, FutureStack, Pekko, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFuturePekkoScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIONettyScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, OxStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainSyncNettyScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, IOStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOVertxScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIONettyScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, ZIOHttp, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOhttpZIOScala3(groupId, addDocumentation, addMetrics).toString()
          case StarterDetails(_, groupId, ZIOStack, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOVertxScala3(groupId, addDocumentation, addMetrics).toString()
          case _ => throw UnsupportedOperationException(s"$starterDetails not supported")
        }
    }
