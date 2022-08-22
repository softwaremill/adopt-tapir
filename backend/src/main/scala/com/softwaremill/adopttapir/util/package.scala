package com.softwaremill.adopttapir

import cats.data.NonEmptyList
import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

package object util {

  type ServerEndpoints = NonEmptyList[ServerEndpoint[Fs2Streams[IO], IO]]

  def constantTimeEquals(s1: String, s2: String): Boolean = {
    val a = s1.getBytes()
    val b = s2.getBytes()
    if (a.length != b.length) return false
    var result = 0
    for (i <- 0 until a.length) {
      result |= a(i) ^ b(i)
    }
    result == 0
  }
}
