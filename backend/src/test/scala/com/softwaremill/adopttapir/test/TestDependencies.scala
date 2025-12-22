package com.softwaremill.adopttapir.test

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.adopttapir.config.Config
import com.softwaremill.adopttapir.Dependencies
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import org.scalatest.{BeforeAndAfterAll, Suite}
import sttp.capabilities.fs2.Fs2Streams
import sttp.client4.{StreamBackend, WebSocketStreamBackend}
import sttp.client4.httpclient.fs2.HttpClientFs2Backend
import sttp.client4.testing.WebSocketStreamBackendStub
import sttp.tapir.server.stub4.TapirWebSocketStreamStubInterpreter

trait TestDependencies extends BeforeAndAfterAll:
  self: Suite with BaseTest =>
  given CorrelationId = CorrelationId.init.unsafeRunSync()

  var dependencies: Dependencies = _
  var releaseDependencies: IO[Unit] = _
  val TestConfig: Config = Config.read.unsafeRunSync()

  private val stub: WebSocketStreamBackendStub[IO, Fs2Streams[IO]] = HttpClientFs2Backend.stub[IO]

  override protected def beforeAll(): Unit =
    super.beforeAll()
    val resources = {
      Dependencies
        .wire(
          config = TestConfig
        )
        .allocated
        .unsafeRunSync()
    }

    dependencies = resources._1
    releaseDependencies = resources._2

  override protected def afterAll(): Unit =
    releaseDependencies.unsafeRunSync()

  private lazy val serverStub: WebSocketStreamBackend[IO, Fs2Streams[IO]] =
    TapirWebSocketStreamStubInterpreter[IO, Fs2Streams[IO]](stub)
      .whenServerEndpointsRunLogic(dependencies.api.allPublicEndpoints)
      .backend()

  lazy val requests = new Requests(serverStub)
