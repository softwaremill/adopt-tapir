package com.softwaremill.adopttapir.starter.files

import cats.syntax.all.*
import better.files.File.newTemporaryDirectory
import better.files.{File => BFile}
import cats.effect.IO
import cats.implicits.toTraverseOps
import com.softwaremill.adopttapir.template.GeneratedFile
import com.softwaremill.adopttapir.util.ZipArchiver

import java.io.File

class FilesManager(config: StorageConfig):

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

  def zipDirectory(directoryFile: File): IO[File] =
    IO.blocking {
      val destination = BFile.newTemporaryFile(prefix = directoryFile.getName + "_", suffix = ".zip")
      ZipArchiver().create(destination.path, directoryFile.toPath)
      destination.toJava
  }

  def deleteFilesAsStatedInConfig(destinationDir: IO[File]): IO[Unit] =
    IO.blocking {
    if config.deleteTempFolder then
      for
        dir <- destinationDir
        _ <- dir.delete().pure[IO]
      yield ()
    else
      ().pure[IO]
  }
