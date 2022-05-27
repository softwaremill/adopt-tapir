package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerImplementation._
import com.softwaremill.adopttapir.starter.StarterDetails

object MainView {

  def getProperMainContent(starterDetails: StarterDetails): String = starterDetails match {
    case StarterDetails.FutureStarterDetails(_, groupId, Akka, _)  => txt.MainFutureAkka(groupId).toString()
    case StarterDetails.FutureStarterDetails(_, groupId, Netty, _) => txt.MainFutureNetty(groupId).toString()
    case StarterDetails.IOStarterDetails(_, groupId, Http4s, _)    => txt.MainIOHttp4s(groupId).toString()
    case StarterDetails.IOStarterDetails(_, groupId, Netty, _)     => txt.MainIONetty(groupId).toString()
    case StarterDetails.ZIOStarterDetails(_, groupId, Http4s, _)   => txt.MainZIOHttp4s(groupId).toString()
    case StarterDetails.ZIOStarterDetails(_, groupId, ZIOHttp, _)  => txt.MainZIOhttpZIO(groupId).toString()
    case _ => throw new UnsupportedOperationException(s"$starterDetails not supported")
  }

}
