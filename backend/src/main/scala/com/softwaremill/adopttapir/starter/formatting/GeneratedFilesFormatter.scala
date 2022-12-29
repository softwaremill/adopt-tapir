package com.softwaremill.adopttapir.starter.formatting

import cats.effect.{IO, Resource}
import cats.effect.std.Dispatcher
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.starter.files.FilesManager
import com.softwaremill.adopttapir.starter.files.StorageConfig
import com.softwaremill.adopttapir.template.{CommonObjectTemplate, GeneratedFile}
import org.scalafmt.interfaces.{Scalafmt, ScalafmtReporter}

import java.io.{OutputStreamWriter, PrintWriter}
import java.nio.file.Path

final case class GeneratedFilesFormatter private (filesManager: FilesManager, scalafmt: Scalafmt)(using CorrelationId) extends FLogging:

  private val ScalaFileExtensions = Set(".scala", ".sbt")

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
        logger.error(s"Cannot find formatting file in generated project, scala files will NOT be formatted!") *>
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

object GeneratedFilesFormatter extends FLogging:

  private def scalafmtReporter(using CorrelationId): Resource[IO, ScalafmtReporter] =
    for dispatcher <- Dispatcher.parallel[IO]
    yield new ScalafmtReporter {
      override def error(file: Path, message: String): Unit = dispatcher.unsafeRunSync(logger.error(s"Error: $file: $message."))

      override def error(file: Path, e: Throwable): Unit = dispatcher.unsafeRunSync(logger.error(s"Error: $file: ", e))

      override def excluded(file: Path): Unit = dispatcher.unsafeRunSync(logger.info(s"File excluded: $file."))

      override def parsedConfig(config: Path, scalafmtVersion: String): Unit =
        dispatcher.unsafeRunSync(logger.info(s"Parsed config (v$scalafmtVersion): $config."))

      override def downloadWriter(): PrintWriter = PrintWriter(System.out)

      override def downloadOutputStreamWriter(): OutputStreamWriter = OutputStreamWriter(System.out)
    }

  private def initScalafmt(using CorrelationId) = scalafmtReporter.flatMap(reporter =>
    Resource.make(
      IO.blocking(
        Scalafmt
          .create(getClass.getClassLoader)
          .withReporter(reporter)
      )
    )(s => IO(s.clear()))
  )

  def create(filesManager: FilesManager)(using CorrelationId): Resource[IO, GeneratedFilesFormatter] =
    initScalafmt.map(GeneratedFilesFormatter(filesManager, _))
