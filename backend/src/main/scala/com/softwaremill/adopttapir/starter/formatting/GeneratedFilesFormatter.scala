package com.softwaremill.adopttapir.starter.formatting

import cats.effect.IO
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.template.{CommonObjectTemplate, GeneratedFile}
import org.scalafmt.interfaces.Scalafmt

import java.nio.file.Path

class GeneratedFilesFormatter(fm: FilesManager) extends FLogging {

  private val ScalaFileExtensions = Set(".scala", ".sbt")

  private lazy val scalafmt = Scalafmt.create(getClass.getClassLoader)

  def format(gfs: List[GeneratedFile]): IO[List[GeneratedFile]] = {
    findGeneratedFormatFile(gfs) match {
      case Some(formatFile) =>
        IO.blocking(fm.createTempDir())
          .bracket { tempDirectory =>
            for {
              tempDir <- tempDirectory
              formatFile <- fm.createFile(tempDir, formatFile)
              formattedFiles <- IO(formatScalaFiles(formatFile.toPath, gfs))
            } yield formattedFiles
          }(release = tempDirectory => fm.deleteFilesAsStatedInConfig(tempDirectory))
      case None =>
        logger.error(s"Cannot find formatting file in generated project, scala files will NOT be formatted!")
        IO(gfs)
    }
  }

  private def findGeneratedFormatFile(gfs: List[GeneratedFile]): Option[GeneratedFile] = {
    gfs.find(_.relativePath.endsWith(CommonObjectTemplate.scalafmtConfigPath))
  }

  private def formatScalaFiles(scalaFormatPath: Path, gfs: List[GeneratedFile]): List[GeneratedFile] = {
    gfs.map {
      case gf if isScalaFilePath(gf.relativePath) =>
        gf.copy(content = scalafmt.format(scalaFormatPath, Path.of(gf.relativePath), gf.content))
      case gf => gf
    }
  }

  private def isScalaFilePath(path: String): Boolean = ScalaFileExtensions.exists(e => path.endsWith(e))
}
