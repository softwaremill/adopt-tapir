package com.softwaremill.adopttapir.starter.api

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.implicits.toBifunctorOps
import com.softwaremill.adopttapir.Fail
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.infrastructure.Json._
import com.softwaremill.adopttapir.starter._
import com.softwaremill.adopttapir.util.ServerEndpoints
import fs2.io.file.Files
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.HeaderNames
import sttp.tapir.CodecFormat

class StarterApi(http: Http, starterService: StarterService) {

  type ContentDispositionValue = String
  type ContentLengthValue = Long

  import http._

  private val starterPath = "starter.zip"

  private val starterEndpoint = {
    val zippedFileStream = streamBinaryBody(Fs2Streams[IO])(CodecFormat.Zip())

    baseEndpoint.post
      .in(starterPath)
      .in(jsonBody[StarterRequest])
      .out(zippedFileStream)
      .out(header(HeaderNames.AccessControlExposeHeaders, HeaderNames.ContentDisposition))
      .out(header[ContentDispositionValue](HeaderNames.ContentDisposition))
      .out(header[ContentLengthValue](HeaderNames.ContentLength))
      .serverLogic[IO] { request =>
        val logicFlow: EitherT[IO, Fail, (fs2.Stream[IO, Byte], ContentDispositionValue, ContentLengthValue)] = for {
          det <- EitherT(IO.pure(FormValidator.validate(request)))
          result <- EitherT.liftF(
            starterService
              .generateZipFile(det)
              .map(file => (toStreamDeleteAfterComplete(file), defineZipFileName(request.projectName), file.length()))
          )
        } yield result

        logicFlow.value
          .map(_.leftMap(http.failToResponseData))
      }
  }

  private def defineZipFileName(projectName: String): ContentDispositionValue = {
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition
    s"attachment; filename=\"$projectName-tapir-starter.zip\""
  }

  private def toStreamDeleteAfterComplete(zippedFile: TapirFile): fs2.Stream[IO, Byte] = {
    Files[IO]
      .readAll(fs2.io.file.Path(zippedFile.getPath))
      .onFinalize(IO.blocking(zippedFile.delete()) >> IO.unit)
  }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        starterEndpoint
      )
      .map(_.tag(starterPath))

}
