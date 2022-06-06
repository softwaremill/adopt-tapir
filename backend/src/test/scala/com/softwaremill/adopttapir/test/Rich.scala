package com.softwaremill.adopttapir.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.DurationInt

object Rich {
  implicit class RichIO[T](t: IO[T]) {
    def unwrap: T = t.unsafeRunTimed(1.minute).get
  }
}
