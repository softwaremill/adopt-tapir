package com.softwaremill.adopttapir.starter

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.metrics.Metrics.generatedStarterCounter
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

import java.io.File

class StarterService(projectGenerator: ProjectGenerator, filesManager: FilesManager, generatedFilesFormatter: GeneratedFilesFormatter)
    extends FLogging {

  def generateZipFile(starterDetails: StarterDetails): IO[File] = {
    logger.info(s"received request: $starterDetails") *>
      IO(generatedFilesFormatter.format(projectGenerator.generate(starterDetails)))
        .flatMap(formattedGeneratedFiles => {
          IO
            .blocking(filesManager.createTempDir())
            .bracket { tempDirectory =>
              for {
                tempDir <- tempDirectory
                _ <- logger.debug("created temp dir: " + tempDir)
                generatedFiles <- formattedGeneratedFiles
                _ <- filesManager.createFiles(tempDir, generatedFiles)
                zippedFile <- filesManager.zipDirectory(tempDir)
                _ <- increaseMetricCounter(starterDetails)
              } yield zippedFile
            }(release = tempDirectory => filesManager.deleteFilesAsStatedInConfig(tempDirectory))
        })
  }

  private def increaseMetricCounter(details: StarterDetails): IO[Unit] = {
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
  }
}
