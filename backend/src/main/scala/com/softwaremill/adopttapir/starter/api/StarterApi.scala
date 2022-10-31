package com.softwaremill.adopttapir.starter.api

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.implicits.toBifunctorOps
import com.softwaremill.adopttapir.Fail
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.infrastructure.Json._
import com.softwaremill.adopttapir.starter.*
import com.softwaremill.adopttapir.starter.content.{ContentService, Node}
import com.softwaremill.adopttapir.util.ServerEndpoints
import fs2.io.file.Files
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.HeaderNames
import sttp.tapir.CodecFormat

class StarterApi(http: Http, starterService: StarterService, contentService: ContentService):

  type ContentDispositionValue = String
  type ContentLengthValue = Long

  import http.*

  private val starterApiTag = "starter"

  private val starterPath = "starter.zip"

  private val starterEndpoint =
    val zippedFileStream = streamBinaryBody(Fs2Streams[IO])(CodecFormat.Zip())

    baseEndpoint.post
      .in(starterPath)
      .in(jsonBody[StarterRequest])
      .out(zippedFileStream)
      .out(header(HeaderNames.AccessControlExposeHeaders, HeaderNames.ContentDisposition))
      .out(header[ContentDispositionValue](HeaderNames.ContentDisposition))
      .out(header[ContentLengthValue](HeaderNames.ContentLength))
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
      .in(jsonBody[StarterRequest])
      .out(jsonBody[List[Node]])
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
