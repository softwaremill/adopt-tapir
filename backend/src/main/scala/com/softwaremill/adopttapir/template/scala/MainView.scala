package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation._
import com.softwaremill.adopttapir.starter.StarterDetails

object MainView {

  def getProperMainContent(starterDetails: StarterDetails): String = starterDetails match {
    case StarterDetails(_, groupId, FutureEffect, Akka, _)  => txt.MainFutureAkka(groupId).toString()
    case StarterDetails(_, groupId, FutureEffect, Netty, _) => txt.MainFutureNetty(groupId).toString()
    case StarterDetails(_, groupId, IOEffect, Http4s, _)    => txt.MainIOHttp4s(groupId).toString()
    case StarterDetails(_, groupId, IOEffect, Netty, _)     => txt.MainIONetty(groupId).toString()
    case StarterDetails(_, groupId, ZIOEffect, Http4s, _)   => txt.MainZIOHttp4s(groupId).toString()
    case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, _)  => txt.MainZIOhttpZIO(groupId).toString()
    case _                                                  => throw new UnsupportedOperationException(s"$starterDetails not supported")
  }

}