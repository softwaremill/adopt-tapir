package com.softwaremill.adopttapir.starter

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.template.{FileTemplate, ProjectTemplate}
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
    val filesToCreate = prepareTemplateFiles(starterDetails)
    IO(os.temp.dir(prefix = config.tempPrefix).toIO)
      .bracket { tempDir =>
        for{
          _ <- logger.debug("created temp dir: " + tempDir.toString)
          _ <- storeFiles(tempDir, filesToCreate)
          // TODO: Storage leak: Zip files needs to be cleaned! In future just add something like IO cron job which will
          //  periodically cleans the data older than 5 min with specific config.tempPrefix.
          dir <- zipDirectory(tempDir)
        } yield dir
      }(release = tempDir => if (config.deleteTempFolder) deleteRecursively(tempDir) else IO.unit)
  }

  private def prepareTemplateFiles(starterDetails: StarterDetails) = {
    List(
      template.getBuildSbt(starterDetails),
      template.getSbtPlugins(starterDetails),
      template.getBuildProperties(),
      template.getMain(starterDetails),
      template.getMainSpec(starterDetails)
    )
  }

  private def storeFiles(destinationFolder: File, filesToCreate: List[FileTemplate]): IO[Unit] = IO {
    filesToCreate.foreach { fileTemplate =>
      os.write.over(
        os.Path(destinationFolder) / fileTemplate.relativePath,
        fileTemplate.content,
        createFolders = true
      )
    }
  }

  private def zipDirectory(directoryFile: File): IO[File] = IO {
    import better.files._
    import better.files.{File => BFile}
    directoryFile.toScala.zip(Deflater.BEST_SPEED).toJava
    directoryFile.toScala.zipTo(
      destination = BFile.newTemporaryFile(prefix = directoryFile.getName + "_", suffix = ".zip"),
      Deflater.BEST_SPEED).toJava
  }

  private def deleteRecursively(tempDir: File) = IO {
    new Directory(tempDir).deleteRecursively()
    ()
  }
}
