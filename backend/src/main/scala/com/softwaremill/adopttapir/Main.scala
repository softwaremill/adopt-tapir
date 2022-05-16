package com.softwaremill.adopttapir

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.infrastructure.{CorrelationId, DB, SetCorrelationIdBackend}
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.util.DefaultClock
import com.typesafe.scalalogging.StrictLogging
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.prometheus.PrometheusBackend

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val config = Config.read
    Config.log(config)

    val xa = new DB(config.db).transactorResource.map(CorrelationId.correlationIdTransactor)

    Dependencies
      .wire(config, xa, DefaultClock)
      .use { case Dependencies(httpApi) =>
        /*
        - allocates the http api resource, and never releases it (so that the http server is available
          as long as our application runs)
         */
        httpApi.resource.use(_ => IO.never)
      }
      .unsafeRunSync()
  }
}
