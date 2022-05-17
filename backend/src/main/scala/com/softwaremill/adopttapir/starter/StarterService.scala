package com.softwaremill.adopttapir.starter

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.util.Clock

import java.io.File

class StarterService(
    clock: Clock,
    config: StarterConfig
) extends FLogging {
  def generateZipFile(starterDetails: StarterDetails): IO[File] = {
    IO {
      new java.io.File("src/main/resources/temporal.zip")
    }
  }
}
