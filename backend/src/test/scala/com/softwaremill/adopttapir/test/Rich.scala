package com.softwaremill.adopttapir.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.DurationInt

object RichIO:
  extension [T](io: IO[T]) def unwrap: T = io.unsafeRunTimed(1.minute).get
