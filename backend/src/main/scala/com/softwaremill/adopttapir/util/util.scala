package com.softwaremill.adopttapir.util

import cats.data.NonEmptyList
import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

type ServerEndpoints = NonEmptyList[ServerEndpoint[Fs2Streams[IO], IO]]
