package com.softwaremill.adopttapir.starter

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.metrics.Metrics.generatedStarterCounter
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

import java.io.File

class StarterService(generatedFilesFormatter: GeneratedFilesFormatter) extends FLogging:

//  export generatedFilesFormatter.filesManager
  private val fm = generatedFilesFormatter.filesManager

  def generateZipFile(starterDetails: StarterDetails): IO[File] =
    logger.info(s"received request: $starterDetails") *>
      IO(generatedFilesFormatter.format(ProjectGenerator.generate(starterDetails)))
        .flatMap(formattedGeneratedFiles => {
          IO
            .blocking(fm.createTempDir())
            .bracket { tempDirectory =>
              for
                tempDir <- tempDirectory
                _ <- logger.debug("created temp dir: " + tempDir)
                generatedFiles <- formattedGeneratedFiles
                _ <- fm.createFiles(tempDir, generatedFiles)
                zippedFile <- fm.zipDirectory(tempDir)
                _ <- increaseMetricCounter(starterDetails)
              yield zippedFile
            }(release = tempDirectory => fm.deleteFilesAsStatedInConfig(tempDirectory))
        })

  private def increaseMetricCounter(details: StarterDetails): IO[Unit] =
    val labelValues = details.productElementNames
      .zip(details.productIterator.toList)
      .filterNot { case (name, _) => Metrics.excludedStarterDetailsFields.contains(name) }
      .map(_._2.toString)
      .toList

    IO(
      generatedStarterCounter
        .labels(labelValues: _*)
        .inc()
    )
