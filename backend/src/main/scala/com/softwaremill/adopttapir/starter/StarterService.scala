package com.softwaremill.adopttapir.starter

import better.files.File.newTemporaryDirectory
import better.files.{FileExtensions, File => BFile}
import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.template.{GeneratedFile, ProjectTemplate}
import com.softwaremill.adopttapir.util.Clock

import java.io.File
import java.util.zip.Deflater
import scala.reflect.io.Directory

class StarterService(
    clock: Clock,
    config: StarterConfig,
    template: ProjectTemplate
) extends FLogging {

  def generateZipFile(starterDetails: StarterDetails): IO[File] = {
    val filesToCreate = generateFiles(starterDetails)
    IO.blocking(newTemporaryDirectory(prefix = config.tempPrefix).toJava)
      .bracket { tempDir =>
        for {
          _ <- logger.debug("created temp dir: " + tempDir.toString)
          _ <- storeFiles(tempDir, filesToCreate)
          dir <- zipDirectory(tempDir)
        } yield dir
      }(release = tempDir => if (config.deleteTempFolder) deleteRecursively(tempDir) else IO.unit)
  }

  private def generateFiles(starterDetails: StarterDetails): List[GeneratedFile] = {
    List(
      template.getBuildSbt(starterDetails),
      template.getSbtPlugins(),
      template.getBuildProperties(),
      template.getMain(starterDetails),
      template.getMainSpec(starterDetails)
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
    directoryFile.toScala
      .zipTo(destination = BFile.newTemporaryFile(prefix = directoryFile.getName + "_", suffix = ".zip"), Deflater.BEST_SPEED)
      .toJava
  }

  private def deleteRecursively(tempDir: File): IO[Unit] = IO.blocking {
    new Directory(tempDir).deleteRecursively()
    ()
  }
}
