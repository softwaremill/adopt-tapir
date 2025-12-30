package com.softwaremill.adopttapir.starter.api

import better.files.{DisposeableExtensions, File}
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.http.Error_OUT
import com.softwaremill.adopttapir.infrastructure.Json.*
import com.softwaremill.adopttapir.starter.api.StackRequest.FutureStack
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.{Jsoniter, ZIOJson}
import com.softwaremill.adopttapir.starter.api.ScalaVersionRequest.Scala2
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterApiTest.{mainPath, validSbtRequest, validScalaCliRequest, validScalaCliSingleFileRequest}
import com.softwaremill.adopttapir.test.RichIO.unwrap
import com.softwaremill.adopttapir.test.{BaseTest, TestDependencies}
import fs2.io.file.Files
import io.circe.jawn
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.scalatest.Assertion
import sttp.client4.{Response, SttpClientException}
import sttp.client4.ResponseException.UnexpectedStatusCode

class StarterApiTest extends BaseTest with TestDependencies {

  "/starter.zip" should "return a zip response with specified files for Sbt builder" in {
    // given
    val req = validSbtRequest

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200
    checkStreamZipContent(response.body) { unpackedDir =>
      unpackedDir.listRecursively.toList.filter(_.isRegularFile).map(_.path.getFileName.toString) should contain theSameElementsAs List(
        "build.properties",
        "plugins.sbt",
        ".scalafmt.conf",
        "build.sbt",
        "EndpointsSpec.scala",
        "Endpoints.scala",
        "Main.scala",
        "sbtx",
        "README.md",
        ".gitignore",
        "logback.xml"
      )
    }
  }

  "/starter.zip" should "return a zip containing sbtx with permissions 755" in {
    // given
    val req = validSbtRequest

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200

    checkStreamZip(response.body) { zippedFile =>
      checkZipEntry(zippedFile)(_.getEntry(s"${req.projectName}/sbtx").getUnixMode shouldBe 493)
    }
  }

  "/starter.zip" should "return a zip response with specified files for ScalaCli builder" in {
    // given
    val req = validScalaCliRequest

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200
    checkStreamZipContent(response.body) { unpackedDir =>
      unpackedDir.listRecursively.toList.filter(_.isRegularFile).map(_.path.getFileName.toString) should contain theSameElementsAs List(
        "project.scala",
        ".scalafmt.conf",
        "EndpointsSpec.scala",
        "Endpoints.scala",
        "Main.scala",
        "README.md",
        ".gitignore",
        "logback.xml"
      )
    }
  }

  "/starter.zip" should "return a zip response with specified files for ScalaCliSingleFile builder" in {
    // given
    val req = validScalaCliSingleFileRequest

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200
    checkStreamZipContent(response.body) { unpackedDir =>
      unpackedDir.listRecursively.toList.filter(_.isRegularFile).map(_.path.getFileName.toString) should contain theSameElementsAs List(
        s"${req.projectName}.scala",
        "README.md"
      )
    }
  }

  for req <- Seq(validSbtRequest, validScalaCliRequest) do {
    it should s"have relative paths associated with groupId in request for .scala files for ${req.builder} builder" in {
      // given
      val groupIdRelativePath = s"${req.groupId.replace('.', '/')}"

      // when
      val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

      // then
      response.code.code shouldBe 200
      checkStreamZipContent(response.body) { unpackedDir =>
        val paths = unpackedDir.listRecursively.toList
          .collect {
            case f: File
                if f.path.toString.endsWith("README.md") ||
                  f.path.toString.endsWith(".scala") && !f.path.endsWith("project.scala") && f.isRegularFile =>
              unpackedDir.relativize(f)
          }
        val root = req.projectName
        paths.map(_.toString) should contain theSameElementsAs List(
          s"$root/README.md",
          s"$root/$mainPath/$groupIdRelativePath/Main.scala",
          s"$root/src/main/scala/$groupIdRelativePath/Endpoints.scala",
          s"$root/src/test/scala/$groupIdRelativePath/EndpointsSpec.scala"
        )
      }
    }
  }

  it should "return request error with information about picking wrong implementation for a stack" in {
    // given
    val request = StarterRequestGenerators.randomStarterRequest().copy(stack = FutureStack, implementation = ZIOHttp)

    // when
    val rootEx = intercept[SttpClientException](requests.requestZip(request))
    val ex = rootEx.cause.asInstanceOf[UnexpectedStatusCode[String]]

    // then
    ex.response.code.code shouldBe 400
    jawn.decode[Error_OUT](ex.body).value.error should include(
      "Picked FutureStack with ZIOHttp - Future stack will work only with: Netty, Vert.X"
    )
  }

  it should "return request error with information about picking wrong stack for a json" in {
    // given
    val request = StarterRequestGenerators.randomStarterRequest().copy(stack = FutureStack, json = ZIOJson)

    // when
    val rootEx = intercept[SttpClientException](requests.requestZip(request))
    val ex = rootEx.cause.asInstanceOf[UnexpectedStatusCode[String]]

    // then
    ex.response.code.code shouldBe 400
    jawn.decode[Error_OUT](ex.body).value.error should include(
      "ZIOJson will work only with ZIO stack"
    )
  }

  it should "return request error with information about wrong projectName " in {
    // given
    val request =
      StarterRequestGenerators.randomStarterRequest().copy(projectName = "Uppercase", stack = FutureStack, implementation = Netty)

    // when
    val rootEx = intercept[SttpClientException](requests.requestZip(request))
    val ex = rootEx.cause.asInstanceOf[UnexpectedStatusCode[String]]

    // then
    ex.response.code.code shouldBe 400
    jawn.decode[Error_OUT](ex.body).value.error should include(
      "Project name: `Uppercase` should match regex: `^[a-z0-9_]$|^[a-z0-9_]+[a-z0-9_-]*[a-z0-9_]+$`"
    )
  }

  def checkStreamZip[A](fileStream: fs2.Stream[IO, Byte])(assertionOnZipFn: File => A): Unit = {
    tempZipFile().use { case tempZipFile =>
      for
        _ <- fileStream
          .through(Files[IO].writeAll(fs2.io.file.Path(tempZipFile.toJava.getPath)))
          .compile
          .drain
        _ <- IO.blocking(assertionOnZipFn(tempZipFile))
      yield ()
    }.unwrap
  }

  def checkStreamZipContent(fileStream: fs2.Stream[IO, Byte])(assertionOnUnpackedDirFn: File => Assertion): Unit = {
    (for
      tempZipFile <- tempZipFile()
      tempDir <- tempDir()
    yield (tempZipFile, tempDir)).use { case (tempZipFile, tempDir) =>
      for
        _ <- fileStream
          .through(Files[IO].writeAll(fs2.io.file.Path(tempZipFile.toJava.getPath)))
          .compile
          .drain
        _ <- IO.blocking(tempZipFile.unzipTo(tempDir))
        _ <- IO.blocking(assertionOnUnpackedDirFn(tempDir))
      yield ()

    }.unwrap
  }

  private def tempZipFile(): Resource[IO, File] = Resource
    .make(IO.blocking(better.files.File.newTemporaryFile("starterApiTest", ".zip")))(file => IO.blocking(file.delete()))

  private def tempDir(): Resource[IO, File] = Resource
    .make(IO.blocking(better.files.File.newTemporaryDirectory("starterApiTest")))(f => IO.blocking(f.delete()))
}

private def checkZipEntry[A](zippedFile: File)(applyFn: ZipFile => A) = {
  for
    channel <- new SeekableInMemoryByteChannel(zippedFile.byteArray).autoClosed
    zipFile <- new ZipFile(channel).autoClosed
  do {
    applyFn(zipFile)
  }
}

object StarterApiTest {
  val validSbtRequest: StarterRequest = StarterRequest(
    projectName = "projectname",
    groupId = "com.softwaremill",
    stack = FutureStack,
    implementation = ServerImplementationRequest.Netty,
    addDocumentation = true,
    addMetrics = false,
    json = Jsoniter,
    scalaVersion = Scala2,
    builder = BuilderRequest.Sbt
  )

  val validScalaCliRequest: StarterRequest = validSbtRequest.copy(builder = BuilderRequest.ScalaCli)
  
  val validScalaCliSingleFileRequest: StarterRequest = validSbtRequest.copy(builder = BuilderRequest.ScalaCliSingleFile)

  val mainPath = "src/main/scala"
  val testPath = "src/test/scala"
}
