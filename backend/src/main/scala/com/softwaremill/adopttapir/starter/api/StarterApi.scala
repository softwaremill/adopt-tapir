package com.softwaremill.adopttapir.starter.api

import cats.data.{EitherT, NonEmptyList}
import cats.effect.IO
import cats.implicits.toBifunctorOps
import com.softwaremill.adopttapir.Fail
import com.softwaremill.adopttapir.http.Http
import com.softwaremill.adopttapir.starter._
import com.softwaremill.adopttapir.util.ServerEndpoints
import fs2.io.file.Files
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.generic.auto._

class StarterApi(http: Http, starterService: StarterService) {
  import http._

  private val starterPath = "starter.zip"

  private val starterEndpoint = baseEndpoint.get
    .in(starterPath)
    .in(query[String]("tapirVersion"))
    .in(jsonBody[StarterRequest])
    .out(streamBinaryBody(Fs2Streams[IO]))
    .serverLogic[IO] { case (tapirVersion, request) =>
      val logicFlow: EitherT[IO, Fail, fs2.Stream[IO, Byte]] = for {
        det <- EitherT(IO.pure(FormValidator.validate(tapirVersion, request)))
        result <- EitherT.liftF(starterService.generateZipFile(det).map(cleanResource))
      } yield result

      logicFlow.value
        .map(_.leftMap(http.failToResponseData))
    }

  private def cleanResource(zippedFile: TapirFile): fs2.Stream[IO, Byte] = {
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
