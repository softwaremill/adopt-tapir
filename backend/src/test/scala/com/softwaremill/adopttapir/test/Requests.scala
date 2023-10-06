package com.softwaremill.adopttapir.test

import cats.effect.IO
import com.softwaremill.adopttapir.starter.api.StarterRequest
import com.softwaremill.adopttapir.test.RichIO.unwrap
import io.circe.syntax.EncoderOps
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.{Response, SttpBackend, UriContext, asStreamUnsafe, basicRequest}

class Requests(backend: SttpBackend[IO, Any with Fs2Streams[IO]]):

  private val basePath = "http://localhost:9090/api/v1"

  def requestZip(request: StarterRequest): Response[fs2.Stream[IO, Byte]] =
    basicRequest
      .post(uri"$basePath/starter.zip")
      .body(request.asJson.noSpaces)
      .response(asStreamUnsafe(Fs2Streams[IO]).getRight)
      .send(backend)
      .unwrap
