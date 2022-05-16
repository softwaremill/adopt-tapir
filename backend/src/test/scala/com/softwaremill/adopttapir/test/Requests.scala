package com.softwaremill.adopttapir.test

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.{SttpBackend, UriContext, asByteArray, basicRequest}

import java.io.File

class Requests(backend: SttpBackend[IO, Any]) extends AnyFlatSpec with Matchers with TestSupport {

  private val basePath = "http://localhost:8080/api/v1"

  def getFile(): File = {

    basicRequest
      .get(uri"$basePath/starter")
      .response(asByteArray)
      .send(backend)
      .map{ response =>
        response.body.asInstanceOf[File]
      }
      .unwrap

  }
}
