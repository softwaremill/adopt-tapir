package com.softwaremill.adopttapir.util

import better.files.{DisposeableExtensions, FileExtensions}
import com.softwaremill.adopttapir.template.SbtProjectTemplate.sbtxFile
import org.apache.commons.compress.archivers.zip.{ZipArchiveEntry, ZipArchiveOutputStream}

import java.nio.file.Path

trait ZipArchiver:
  protected val fileNameToPermissionsInHex: Map[String, Int]

  def create(archivedPath: Path, directoryToZip: Path): Unit


object ZipArchiver:
  val chmod755: Int = 0x1ed // 0755 written in hex as scala doesn't have octal notation

  def apply(fileNameToPermissions: Map[String, Int] = Map(sbtxFile -> chmod755)): ZipArchiver = new ZipArchiver() {
    override val fileNameToPermissionsInHex: Map[String, Int] = fileNameToPermissions

    override def create(archivedPath: Path, directoryToZip: Path): Unit = {
      val dir = directoryToZip.toFile.toScala
      val files = if dir.isDirectory then dir.children else Iterator(dir)

      for
        output <- new ZipArchiveOutputStream(archivedPath).autoClosed
        input <- files
        file <- input.walk()
        name = input.parent.relativize(file).toString
      do {
        val relativeName = name.stripSuffix(file.fileSystem.getSeparator)
        val entryName = if file.isDirectory then s"$relativeName/" else relativeName

        val entry: ZipArchiveEntry = output.createArchiveEntry(file.toJava, entryName).asInstanceOf[ZipArchiveEntry]
        fileNameToPermissionsInHex.get(entryName).foreach {
          entry.setUnixMode
        }

        output.putArchiveEntry(entry)
        if file.isRegularFile then output.write(file.byteArray)
        output.closeArchiveEntry()
      }
    }
  }

