package com.softwaremill.adopttapir

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.config.Config
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val config = Config.read
    Config.log(config)

    Dependencies
      .wire(config)
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
