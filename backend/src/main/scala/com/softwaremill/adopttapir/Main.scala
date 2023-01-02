package com.softwaremill.adopttapir

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.infrastructure.CorrelationId

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    val server = for
      given CorrelationId <- Resource.eval(CorrelationId.init)
      config <- Resource.eval(Config.read)
      dependencies <- Dependencies.wire(config)
      _ <- dependencies.api.resource
    yield ExitCode.Success

    server.useForever
