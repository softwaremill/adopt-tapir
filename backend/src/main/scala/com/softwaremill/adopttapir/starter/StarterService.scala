package com.softwaremill.adopttapir.starter

import better.files.File.newTemporaryDirectory
import better.files.{File => BFile}
import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
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
              dir <- zipDirectory(tempDir)
            } yield dir
          }(release = tempDir => if (config.deleteTempFolder) deleteRecursively(tempDir) else IO.unit)
      }

  }

  private def generateFiles(starterDetails: StarterDetails): List[GeneratedFile] = {
    List(
      template.getBuildSbt(starterDetails),
      template.getBuildProperties,
      template.getMain(starterDetails),
      template.getEndpoints(starterDetails),
      template.getEndpointsSpec(starterDetails),
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

  private def deleteRecursively(tempDir: File): IO[Unit] = IO.blocking {
    new Directory(tempDir).deleteRecursively()
    ()
  }
}
