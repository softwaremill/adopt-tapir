package com.softwaremill.adopttapir.logging

import cats.effect.IO
import com.softwaremill.adopttapir.infrastructure.CorrelationId
import com.typesafe.scalalogging.Logger
import org.slf4j.{LoggerFactory, MDC}
import cats.syntax.all.*
trait FLogging:
  private val delegate = Logger(LoggerFactory.getLogger(getClass.getName))
  protected def logger: FLogger = new FLogger(delegate)

class FLogger private[logging] (delegate: Logger):
  private val MDCKey = "cid"
  private def withMDC[T](t: => T)(using cid: CorrelationId): IO[T] =
    val iot = IO(t)

    cid.get.flatMap { cid =>
      cid
        .fold(iot)(x => IO(MDC.put(MDCKey, x)) *> iot)
        .guarantee(IO(MDC.remove(MDCKey)))
    }

  def debug(message: String)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.debug(message))
  def debug(message: String, cause: Throwable)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.debug(message, cause))
  def info(message: String)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.info(message))
  def info(message: String, cause: Throwable)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.info(message, cause))
  def warn(message: String)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.warn(message))
  def warn(message: String, cause: Throwable)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.warn(message, cause))
  def error(message: String)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.error(message))
  def error(message: String, cause: Throwable)(using cid: CorrelationId): IO[Unit] = withMDC(delegate.error(message, cause))
