package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation._
import com.softwaremill.adopttapir.starter.StarterDetails

object MainView {

  def getProperMainContent(starterDetails: StarterDetails): String = starterDetails match {
    case StarterDetails(_, groupId, FutureEffect, Akka, _, addDocumentation) => txt.MainFutureAkka(groupId, addDocumentation).toString()
    case StarterDetails(_, groupId, FutureEffect, Netty, _, addDocumentation) =>
      txt.MainFutureNetty(groupId, addDocumentation).toString()
    case StarterDetails(_, groupId, IOEffect, Http4s, _, addDocumentation)   => txt.MainIOHttp4s(groupId, addDocumentation).toString()
    case StarterDetails(_, groupId, IOEffect, Netty, _, addDocumentation)    => txt.MainIONetty(groupId, addDocumentation).toString()
    case StarterDetails(_, groupId, ZIOEffect, Http4s, _, addDocumentation)  => txt.MainZIOHttp4s(groupId, addDocumentation).toString()
    case StarterDetails(_, groupId, ZIOEffect, ZIOHttp, _, addDocumentation) => txt.MainZIOhttpZIO(groupId, addDocumentation).toString()
    case _ => throw new UnsupportedOperationException(s"$starterDetails not supported")
  }

}
