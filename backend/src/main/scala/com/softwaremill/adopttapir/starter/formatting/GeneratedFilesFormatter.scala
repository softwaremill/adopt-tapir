package com.softwaremill.adopttapir.starter.formatting

import cats.effect.IO
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.template.{CommonObjectTemplate, GeneratedFile}
import com.typesafe.scalalogging.StrictLogging
import org.scalafmt.interfaces.{Scalafmt, ScalafmtReporter}

import java.io.{OutputStreamWriter, PrintWriter}
import java.nio.file.Path

final case class GeneratedFilesFormatter(filesManager: FilesManager) extends StrictLogging:

  private val ScalaFileExtensions = Set(".scala", ".sbt")

  private lazy val scalafmtReporter =
    new ScalafmtReporter {
      override def error(file: Path, message: String): Unit = logger.error(s"Error: $file: $message.")

      override def error(file: Path, e: Throwable): Unit = logger.error(s"Error: $file: ", e)

      override def excluded(file: Path): Unit = logger.info(s"File excluded: $file.")

      override def parsedConfig(config: Path, scalafmtVersion: String): Unit =
        logger.info(s"Parsed config (v$scalafmtVersion): $config.")

      override def downloadWriter(): PrintWriter = new PrintWriter(System.out)

      override def downloadOutputStreamWriter(): OutputStreamWriter = new OutputStreamWriter(System.out)
    }

  private lazy val scalafmt =
    Scalafmt
      .create(getClass.getClassLoader)
      .withReporter(scalafmtReporter)

  def format(gfs: List[GeneratedFile]): IO[List[GeneratedFile]] =
    findGeneratedFormatFile(gfs) match {
      case Some(formatFile) =>
        IO.blocking(filesManager.createTempDir())
          .bracket { tempDirectory =>
            for
              tempDir <- tempDirectory
              formatFile <- filesManager.createFile(tempDir, formatFile)
              formattedFiles <- IO(formatScalaFiles(formatFile.toPath, gfs))
            yield formattedFiles
          }(release = tempDirectory => filesManager.deleteFilesAsStatedInConfig(tempDirectory))
      case None =>
        logger.error(s"Cannot find formatting file in generated project, scala files will NOT be formatted!")
        IO(gfs)
    }

  private def findGeneratedFormatFile(gfs: List[GeneratedFile]): Option[GeneratedFile] =
    gfs.find(_.relativePath.endsWith(CommonObjectTemplate.scalafmtConfigPath))

  private def formatScalaFiles(scalaFormatPath: Path, gfs: List[GeneratedFile]): List[GeneratedFile] =
    gfs.map {
      case gf if isScalaFilePath(gf.relativePath) =>
        gf.copy(content = scalafmt.format(scalaFormatPath, Path.of(gf.relativePath), gf.content))
      case gf => gf
    }

  private def isScalaFilePath(path: String): Boolean = ScalaFileExtensions.exists(e => path.endsWith(e))
