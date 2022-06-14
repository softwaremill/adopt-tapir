package com.softwaremill.adopttapir

import java.util.Locale
import cats.data.NonEmptyList
import cats.effect.IO
import com.softwaremill.tagging._
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tsec.common.SecureRandomId

package object util {
  type Id = SecureRandomId

  implicit class RichString(val s: String) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
    def lowerCased: String @@ LowerCased = s.toLowerCase(Locale.ENGLISH).taggedWith[LowerCased]
  }

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
