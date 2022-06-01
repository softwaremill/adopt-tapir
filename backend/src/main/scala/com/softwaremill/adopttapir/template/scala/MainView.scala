package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation._
import com.softwaremill.adopttapir.starter.StarterDetails

object MainView {

  def getProperMainContent(starterDetails: StarterDetails): String = starterDetails match {
    case StarterDetails(_, groupId, FutureEffect, Akka, _, documentationAdded) => txt.MainFutureAkka(groupId, documentationAdded).toString()
    case StarterDetails(_, groupId, FutureEffect, Netty, _, documentationAdded) =>
      txt.MainFutureNetty(groupId, documentationAdded).toString()
    case StarterDetails(_, groupId, IOEffect, Http4s, _, documentationAdded)   => txt.MainIOHttp4s(groupId, documentationAdded).toString()
    case StarterDetails(_, groupId, IOEffect, Netty, _, documentationAdded)    => txt.MainIONetty(groupId, documentationAdded).toString()
    case StarterDetails(_, groupId, ZIOEffect, Http4s, _, documentationAdded)  => txt.MainZIOHttp4s(groupId, documentationAdded).toString()
    case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, _, documentationAdded) => txt.MainZIOhttpZIO(groupId, documentationAdded).toString()
    case _ => throw new UnsupportedOperationException(s"$starterDetails not supported")
  }

}
