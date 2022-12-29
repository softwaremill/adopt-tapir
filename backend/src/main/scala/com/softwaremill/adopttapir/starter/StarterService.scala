package com.softwaremill.adopttapir.starter

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator
import cats.syntax.all.*

import java.io.File

class StarterService(generatedFilesFormatter: GeneratedFilesFormatter, filesManager: FilesManager)(using Metrics) extends FLogging:

  def generateZipFile(starterDetails: StarterDetails): IO[File] =
    for {
      _ <- logger.info(s"received request: $starterDetails")
      generatedFiles <- ProjectGenerator.generate(starterDetails).liftTo[IO]
      formattedGeneratedFiles <- generatedFilesFormatter.format(generatedFiles)
      file <- IO
        .blocking(filesManager.createTempDir())
        .bracket { tempDirectory =>
          for
            tempDir <- tempDirectory
            _ <- logger.debug("created temp dir: " + tempDir)
            _ <- filesManager.createFiles(tempDir, formattedGeneratedFiles)
            zippedFile <- filesManager.zipDirectory(tempDir)
            _ <- Metrics.increaseZipGenerationMetricCounter(starterDetails)
          yield zippedFile
        }(release = tempDirectory => filesManager.deleteFilesAsStatedInConfig(tempDirectory))
    } yield file
