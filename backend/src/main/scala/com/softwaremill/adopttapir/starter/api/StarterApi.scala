package com.softwaremill.adopttapir.starter.api

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.syntax.all.*
import com.softwaremill.adopttapir.Fail
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.starter.*
import com.softwaremill.adopttapir.starter.content.{ContentService, Node}
import com.softwaremill.adopttapir.util.ServerEndpoints
import fs2.io.file.Files
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.HeaderNames
import sttp.tapir.CodecFormat
import sttp.tapir.EndpointIO.Example

class StarterApi(http: Http, starterService: StarterService, contentService: ContentService):

  type ContentDispositionValue = String
  type ContentLengthValue = Long

  import http.*

  private val starterApiTag = "starter"

  private val starterPath = "starter.zip"

  private val starterRequestJsonBody = jsonBody[StarterRequest]
    .description("Parameters defining the stack for the tapir app template")
    .examples(
      List(
        Example[StarterRequest](
          value = StarterRequest(
            projectName = "my-http-app",
            groupId = "com.softwaremill",
            stack = StackRequest.OxStack,
            implementation = ServerImplementationRequest.Netty,
            addDocumentation = true,
            addMetrics = true,
            json = JsonImplementationRequest.Circe,
            scalaVersion = ScalaVersionRequest.Scala3,
            builder = BuilderRequest.Sbt
          ),
          name = "Direct style stack with ox and Netty".some,
          summary = none
        ),
        Example[StarterRequest](
          value = StarterRequest(
            projectName = "my-http-app",
            groupId = "com.softwaremill",
            stack = StackRequest.ZIOStack,
            implementation = ServerImplementationRequest.ZIOHttp,
            addDocumentation = true,
            addMetrics = true,
            json = JsonImplementationRequest.ZIOJson,
            scalaVersion = ScalaVersionRequest.Scala3,
            builder = BuilderRequest.Sbt
          ),
          name = "ZIO stack".some,
          summary = none
        ),
        Example[StarterRequest](
          value = StarterRequest(
            projectName = "my-http-app",
            groupId = "com.softwaremill",
            stack = StackRequest.FutureStack,
            implementation = ServerImplementationRequest.Pekko,
            addDocumentation = true,
            addMetrics = true,
            json = JsonImplementationRequest.Circe,
            scalaVersion = ScalaVersionRequest.Scala3,
            builder = BuilderRequest.Sbt
          ),
          name = "Scala Future stack with Pekko".some,
          summary = none
        ),
        Example[StarterRequest](
          value = StarterRequest(
            projectName = "my-http-app",
            groupId = "com.softwaremill",
            stack = StackRequest.IOStack,
            implementation = ServerImplementationRequest.Http4s,
            addDocumentation = true,
            addMetrics = true,
            json = JsonImplementationRequest.Circe,
            scalaVersion = ScalaVersionRequest.Scala3,
            builder = BuilderRequest.Sbt
          ),
          name = "Cats Effect IO stack with http4s".some,
          summary = none
        )
      )
    )

  private val starterEndpoint =
    val zippedFileStream = streamBinaryBody(Fs2Streams[IO])(CodecFormat.Zip())

    baseEndpoint.post
      .in(starterPath)
      .in(starterRequestJsonBody)
      .out(zippedFileStream)
      .out(header(HeaderNames.AccessControlExposeHeaders, HeaderNames.ContentDisposition))
      .out(header[ContentDispositionValue](HeaderNames.ContentDisposition))
      .out(header[ContentLengthValue](HeaderNames.ContentLength))
      .description("Fetches starter template based on provided parameters as a zip archive.")
      .serverLogic[IO] { (request: StarterRequest) =>
        val result: EitherT[IO, Fail, (fs2.Stream[IO, Byte], ContentDispositionValue, ContentLengthValue)] = for
          starterDetailsOrFail <- EitherT(IO.pure(FormValidator.validate(request)))
          zipFileOrFail <- EitherT.liftF(
            starterService
              .generateZipFile(starterDetailsOrFail)
              .map(file => (toStreamDeleteAfterComplete(file), defineZipFileName(request.projectName), file.length()))
          )
        yield zipFileOrFail

        result.value
          .map(_.leftMap(http.failToResponseData))
      }

  private def defineZipFileName(projectName: String): ContentDispositionValue =
    s"attachment; filename=\"$projectName-tapir-starter.zip\""

  private def toStreamDeleteAfterComplete(zippedFile: TapirFile): fs2.Stream[IO, Byte] =
    Files[IO]
      .readAll(fs2.io.file.Path(zippedFile.getPath))
      .onFinalize(IO.blocking(zippedFile.delete()) >> IO.unit)

  private val contentPath = "content"

  private val contentEndpoint =
    baseEndpoint.post
      .in(contentPath)
      .in(starterRequestJsonBody)
      .out(jsonBody[List[Node]])
      .description(
        "Fetches starter template as a JSON file tree instead of a zip archive, useful to preview the template contents."
      )
      .serverLogic[IO] { (request: StarterRequest) =>
        val result: EitherT[IO, Fail, List[Node]] = for
          starterDetailsOrFail <- EitherT(IO.pure(FormValidator.validate(request)))
          nodeOrFail <- EitherT.liftF(contentService.generateContentTree(starterDetailsOrFail).map(_.content))
        yield nodeOrFail

        result.value
          .map(_.leftMap(http.failToResponseData))
      }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        starterEndpoint.tag(starterApiTag),
        contentEndpoint.tag(starterApiTag)
      )

end StarterApi
