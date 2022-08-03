package com.softwaremill.adopttapir.starter

import better.files.FileExtensions
import cats.effect.IO
import com.softwaremill.adopttapir.template.SbtProjectTemplate
import com.typesafe.scalalogging.LazyLogging
import org.scalafmt.interfaces.Scalafmt

import java.io.File

object FormatScalaFiles extends LazyLogging {

  private val ScalaFileExtensions = Set(".scala", ".sbt")

  private lazy val scalafmt = Scalafmt.create(getClass.getClassLoader)

  def apply(directory: File): IO[Unit] = IO.blocking {
    val scalafmtConfig = directory.toScala / SbtProjectTemplate.ScalafmtConfigFile

    directory.toScala.list(_.extension.exists(ScalaFileExtensions.contains)).foreach { file =>
      logger.debug(s"Formatting ${file.path}")
      val formattedContents = scalafmt.format(scalafmtConfig.path, file.path, file.contentAsString)
      file.overwrite(formattedContents)
    }
  }
}
