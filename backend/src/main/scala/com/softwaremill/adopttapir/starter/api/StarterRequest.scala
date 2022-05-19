package com.softwaremill.adopttapir.starter.api

import com.softwaremill.adopttapir.starter.ServerImplementation

case class StarterRequest (projectName: String, groupId: String, effect: Effect, serverImplementation: ServerImplementation)

sealed trait Effect

object Effect{
  case object IOEffect extends Effect
  case object FutureEffect extends Effect
  case object ZioEffect extends Effect
}
