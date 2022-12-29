package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.*
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterDetails}
import cats.syntax.all.*

object MainView:

  def getProperMainContent(starterDetails: StarterDetails): Either[UnsupportedOperationException, String] =
    starterDetails.scalaVersion match {
      case ScalaVersion.Scala2 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureNetty(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, FutureEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureVertx(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOHttp4s(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIONetty(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOVertx(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOHttp4s(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIONetty(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOhttpZIO(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOVertx(groupId, addDocumentation, addMetrics).toString().asRight
          case _ => UnsupportedOperationException(s"$starterDetails not supported").asLeft
        }
      case ScalaVersion.Scala3 =>
        starterDetails match {
          case StarterDetails(_, groupId, FutureEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureNettyScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, FutureEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainFutureVertxScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIONettyScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, IOEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainIOVertxScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, Http4s, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOHttp4sScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, Netty, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIONettyScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOhttpZIOScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case StarterDetails(_, groupId, ZIOEffect, VertX, addDocumentation, addMetrics, _, _, _) =>
            txt.MainZIOVertxScala3(groupId, addDocumentation, addMetrics).toString().asRight
          case _ => UnsupportedOperationException(s"$starterDetails not supported").asLeft
        }
    }
