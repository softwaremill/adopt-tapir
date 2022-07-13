package com.softwaremill.adopttapir.starter

import better.files.File.newTemporaryDirectory
import better.files.{File => BFile}
import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.metrics.Metrics.generatedStarterCounter
import com.softwaremill.adopttapir.template.ProjectTemplate.legalizeGroupId
import com.softwaremill.adopttapir.template.{GeneratedFile, ProjectTemplate}
import com.softwaremill.adopttapir.util.ZipArchiver

import java.io.File
import scala.reflect.io.Directory

class StarterService(
    config: StarterConfig,
    template: ProjectTemplate
) extends FLogging {

  def generateZipFile(starterDetails: StarterDetails): IO[File] = {
    logger.info(s"received request: $starterDetails") *>
      IO(generateFiles(starterDetails)).flatMap { filesToCreate =>
        IO.blocking(newTemporaryDirectory(prefix = config.tempPrefix).toJava)
          .bracket { tempDir =>
            for {
              _ <- logger.debug("created temp dir: " + tempDir.toString)
              _ <- storeFiles(tempDir, filesToCreate)
              _ <- FormatScalaFiles(tempDir)
              zippedFile <- zipDirectory(tempDir)
              _ <- increaseMetricCounter(starterDetails)
            } yield zippedFile
          }(release = tempDir => if (config.deleteTempFolder) deleteRecursively(tempDir) else IO.unit)
      }

  }

  protected def generateFiles(starterDetails: StarterDetails): List[GeneratedFile] = {
    List(
      template.getBuildSbt(starterDetails),
      template.getBuildProperties,
      template.getMain(legalizeGroupId(starterDetails)),
      template.getEndpoints(legalizeGroupId(starterDetails)),
      template.getEndpointsSpec(legalizeGroupId(starterDetails)),
      template.pluginsSbt,
      template.scalafmtConf,
      template.sbtx,
      template.README
    )
  }

  private def storeFiles(destinationFolder: File, filesToCreate: List[GeneratedFile]): IO[Unit] = IO.blocking {
    filesToCreate.foreach { generatedFile =>
      val file = BFile(destinationFolder.getPath, generatedFile.relativePath)
      file.parent.createDirectories()
      file.overwrite(generatedFile.content)
    }
  }

  private def zipDirectory(directoryFile: File): IO[File] = IO.blocking {
    val destination = BFile.newTemporaryFile(prefix = directoryFile.getName + "_", suffix = ".zip")
    ZipArchiver().create(destination.path, directoryFile.toPath)
    destination.toJava
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

  private def deleteRecursively(tempDir: File): IO[Unit] = IO.blocking {
    new Directory(tempDir).deleteRecursively()
    ()
  }
}
