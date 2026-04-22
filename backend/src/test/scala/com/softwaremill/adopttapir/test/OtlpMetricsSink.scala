package com.softwaremill.adopttapir.test

import cats.effect.{Deferred, IO, Resource}
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.{HttpApp, Method, Response, Status}

trait OtlpMetricsSink:
  def port: Int
  def awaitFirstPush: IO[Array[Byte]]

object OtlpMetricsSink:
  val resource: Resource[IO, OtlpMetricsSink] =
    for
      firstPush <- Resource.eval(Deferred[IO, Array[Byte]])
      app = HttpApp[IO] { req =>
        if req.method == Method.POST && req.uri.path.renderString == "/v1/metrics" then
          req.body.compile.to(Array).flatMap { bytes =>
            firstPush.complete(bytes).attempt.as(Response[IO](Status.Ok))
          }
        else IO.pure(Response[IO](Status.NotFound))
      }
      server <- EmberServerBuilder
        .default[IO]
        .withHost(host"localhost")
        .withPort(port"0")
        .withHttpApp(app)
        .build
    yield new OtlpMetricsSink:
      override val port: Int = server.address.getPort
      override val awaitFirstPush: IO[Array[Byte]] = firstPush.get
