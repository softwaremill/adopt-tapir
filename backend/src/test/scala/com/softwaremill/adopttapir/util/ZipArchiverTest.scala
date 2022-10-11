package com.softwaremill.adopttapir.util

import better.files.{DisposeableExtensions, File}
import com.softwaremill.adopttapir.test.BaseTest
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.scalatest.BeforeAndAfter

import scala.util.Try

class ZipArchiverTest extends BaseTest with BeforeAndAfter {
  var dirToZip: File = _

  before {
    dirToZip = better.files.File.newTemporaryDirectory()
  }

  after {
    Try(dirToZip.delete())
  }

  it should "set unix file permission for zipped file if it is specified on Map" in {
    // given
    val file = dirToZip / "file.txt"
    file.overwrite("hello world")
    val zippedFile: File = better.files.File.newTemporaryFile("ZipArchiverTest", ".zip").deleteOnExit()

    // when
    ZipArchiver(Map(file.name -> ZipArchiver.chmod755)).create(zippedFile.path, dirToZip.path)

    // then
    checkZipEntry(file.name, zippedFile)(_.getEntry(file.name).getUnixMode shouldBe ZipArchiver.chmod755)
  }

  it should "not set unix file permission if it is NOT specified on Map" in {
    // given
    val file = dirToZip / "file.txt"
    file.overwrite("hello world")
    val zippedFile: File = better.files.File.newTemporaryFile("ZipArchiverTest", ".zip").deleteOnExit()

    // when
    ZipArchiver(Map("otherFile" -> ZipArchiver.chmod755)).create(zippedFile.path, dirToZip.path)

    // then
    checkZipEntry(file.name, zippedFile)(_.getEntry(file.name).getUnixMode shouldBe 0)
  }

  private def checkZipEntry[A](filename: String, zippedFile: File)(applyFn: ZipFile => A) = {
    for
      channel <- new SeekableInMemoryByteChannel(zippedFile.byteArray).autoClosed
      zipFile <- new ZipFile(channel).autoClosed
    do {
      applyFn(zipFile)
    }
  }
}
