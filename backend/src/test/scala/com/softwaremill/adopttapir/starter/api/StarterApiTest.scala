package com.softwaremill.adopttapir.starter.api

import better.files.File
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.http.Error_OUT
import com.softwaremill.adopttapir.starter.api.EffectRequest.FutureEffect
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, ZIOHttp}
import com.softwaremill.adopttapir.starter.api.StarterApiTest.{mainPath, validRequest}
import com.softwaremill.adopttapir.test.Rich.RichIO
import com.softwaremill.adopttapir.test.{BaseTest, TestDependencies}
import fs2.io.file.Files
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax.EncoderOps
import org.scalatest.Assertion
import sttp.client3.{HttpError, Response}

class StarterApiTest extends BaseTest with TestDependencies {

  "/starter.zip" should "return a zip response with specified files" in {
    // given
    val req = validRequest

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200
    checkStreamZipContent(response.body) { unpackedDir =>
      unpackedDir.listRecursively.toList.filter(_.isRegularFile).map(_.path.getFileName.toString) should contain theSameElementsAs List(
        "build.properties",
        "build.sbt",
        "EndpointsSpec.scala",
        "Endpoints.scala",
        "Main.scala"
      )
    }
  }

  it should "have relative paths associated with groupId in request for .scala files" in {
    // given
    val req = validRequest
    val groupIdRelativePath = s"${req.groupId.replace('.', '/')}"

    // when
    val response: Response[fs2.Stream[IO, Byte]] = requests.requestZip(req)

    // then
    response.code.code shouldBe 200
    checkStreamZipContent(response.body) { unpackedDir =>
      val paths = unpackedDir.listRecursively.toList
        .collect {
          case f: File if f.path.toString.endsWith(".scala") && f.isRegularFile =>
            unpackedDir.relativize(f)
        }
      paths.map(_.toString) should contain theSameElementsAs List(
        s"$mainPath/$groupIdRelativePath/Main.scala",
        s"src/main/scala/$groupIdRelativePath/Endpoints.scala",
        s"src/test/scala/$groupIdRelativePath/EndpointsSpec.scala"
      )
    }

  }

  it should "return request error with information about picking wrong implementation for an effect" in {
    // given
    val request = StarterRequestGenerators.randomStarterRequest().copy(effect = FutureEffect, implementation = ZIOHttp)

    // when
    val ex = intercept[HttpError[String]](requests.requestZip(request))

    // then
    ex.statusCode.code shouldBe 400
    parser.parse(ex.body).value shouldBe Error_OUT(
      "Picked FutureEffect with ZIOHttp - Future effect will work only with Akka and Netty"
    ).asJson
  }

  it should "return request error with information about wrong projectName " in {
    // given
    val request =
      StarterRequestGenerators.randomStarterRequest().copy(projectName = "Uppercase", effect = FutureEffect, implementation = Akka)

    // when
    val ex = intercept[HttpError[String]](requests.requestZip(request))

    // then
    ex.statusCode.code shouldBe 400
    parser.parse(ex.body).value shouldBe Error_OUT(
      "Project name: `Uppercase` should match regex: `[a-z0-9]+`"
    ).asJson
  }

  def checkStreamZipContent(fileStream: fs2.Stream[IO, Byte])(assertionOnUnpackedDirFn: File => Assertion): Unit = {
    (for {
      tempZipFile <- tempZipFile()
      tempDir <- tempDir()
    } yield (tempZipFile, tempDir)).use { case (tempZipFile, tempDir) =>
      for {
        _ <- fileStream
          .through(Files[IO].writeAll(fs2.io.file.Path(tempZipFile.toJava.getPath)))
          .compile
          .drain
        _ <- IO.blocking(tempZipFile.unzipTo(tempDir))
        _ <- IO.blocking(assertionOnUnpackedDirFn(tempDir))
      } yield ()

    }.unwrap
  }

  private def tempZipFile(): Resource[IO, File] = Resource
    .make(IO.blocking(better.files.File.newTemporaryFile("starterApiTest", ".zip")))(file => IO.blocking(file.delete()))

  private def tempDir(): Resource[IO, File] = Resource
    .make(IO.blocking(better.files.File.newTemporaryDirectory("starterApiTest")))(f => IO.blocking(f.delete()))
}

object StarterApiTest {
  val validRequest: StarterRequest = StarterRequest(
    projectName = "projectname",
    groupId = "com.softwaremill",
    effect = FutureEffect,
    implementation = ServerImplementationRequest.Akka,
    "1.0.0-RC2",
    addDocumentation = true
  )

  val mainPath = "src/main/scala"
  val testPath = "src/test/scala"
}
