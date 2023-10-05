package com.softwaremill.adopttapir.starter.files

import better.files.File as BFile
import better.files.File.newTemporaryDirectory
import cats.effect.IO
import cats.syntax.all.*
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.template.GeneratedFile
import com.softwaremill.adopttapir.util.ZipArchiver

import java.io.File

class FilesManager(config: StorageConfig) extends FLogging:

  def createTempDir(): IO[File] =
    IO.blocking(newTemporaryDirectory(prefix = config.tempPrefix).toJava)

  def createFiles(destinationDir: File, filesToCreate: List[GeneratedFile]): IO[List[File]] =
    filesToCreate.map(gf => { createFile(destinationDir, gf) }).sequence

  def createFile(destinationDir: File, fileToCreate: GeneratedFile): IO[File] =
    IO.blocking {
      val file = BFile(destinationDir.getPath, fileToCreate.relativePath)
      file.parent.createDirectories()
      file.overwrite(fileToCreate.content)
      file.toJava
    }

  def zipDirectory(directoryFile: File, zipRootDirName: String): IO[File] =
    IO.blocking {
      val destination = BFile.newTemporaryFile(prefix = directoryFile.getName + "_", suffix = ".zip")
      ZipArchiver().create(destination.path, directoryFile.toPath, zipRootDirName)
      destination.toJava
    }

  def deleteFilesAsStatedInConfig(destinationDir: IO[File])(using cid: CorrelationId): IO[Unit] = {
    for
      dir <- destinationDir
      deleted <- IO.blocking(dir.delete())
      _ <- logger.info(s"The $dir deletion ${if (deleted) "succeeded" else "failed"}")
    yield ()
  }.whenA(config.deleteTempFolder)
