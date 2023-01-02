package com.softwaremill.adopttapir.infrastructure

import cats.effect.std.Random
import cats.effect.{IO, IOLocal}
import sttp.capabilities.Effect
import sttp.client3.*
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}
import cats.syntax.all.*

final class CorrelationId private (localCid: IOLocal[Option[String]], random: Random[IO]):

  lazy val chars = 'A' to 'Z'
  private def generate(): IO[String] =
    def randomUpperCaseChar() = random.nextIntBounded(chars.size).map(chars.apply)
    def segment = randomUpperCaseChar().replicateA(3).map(_.mkString)
    segment.replicateA(3).map(_.mkString("-"))

  def get: IO[Option[String]] = localCid.get
  def set(v: Option[String]): IO[Unit] = localCid.set(v)
  def setOrNew(v: Option[String]): IO[Unit] = v.fold(generate())(_.pure[IO]).flatMap(cid => localCid.set(cid.some))

object CorrelationId:

  def init: IO[CorrelationId] = for
    random <- Random.scalaUtilRandom[IO]
    local <- IOLocal[Option[String]](None)
  yield CorrelationId(local, random)

//** An sttp backend wrapper, which sets the current correlation id on all outgoing requests. */
class SetCorrelationIdBackend[P](delegate: SttpBackend[IO, P])(using correlationId: CorrelationId) extends SttpBackend[IO, P]:
  import CorrelationIdInterceptor.*

  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] =
    correlationId.get
      .map {
        case Some(cid) => request.header(CorrelationIdInterceptor.HeaderName, cid)
        case None      => request
      }
      .flatMap(delegate.send)

  override def close(): IO[Unit] = delegate.close()

  override def responseMonad: MonadError[IO] = delegate.responseMonad

/** A tapir interceptor, which reads the correlation id from the headers; if it's absent, generates a new one. */
class CorrelationIdInterceptor private (using correlationId: CorrelationId) extends RequestInterceptor[IO]:
  import CorrelationIdInterceptor.*

  override def apply[R, B](
      responder: Responder[IO, B],
      requestHandler: EndpointInterceptor[IO] => RequestHandler[IO, R, B]
  ): RequestHandler[IO, R, B] =
    RequestHandler.from { case (request, endpoints, monad) =>
      val set = correlationId.setOrNew(request.header(HeaderName))
      set >> requestHandler(EndpointInterceptor.noop)(request, endpoints)(monad)
    }

object CorrelationIdInterceptor:
  val HeaderName: String = "X-Correlation-ID"
  def create(using c: CorrelationId) = CorrelationIdInterceptor()
