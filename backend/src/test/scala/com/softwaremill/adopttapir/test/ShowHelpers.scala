package com.softwaremill.adopttapir.test

import cats.Show
import com.softwaremill.adopttapir.starter.StarterDetails

import java.io.{PrintWriter, StringWriter}
import scala.util.Using

object ShowHelpers:
  implicit lazy val detailsShow: Show[StarterDetails] = Show.show[StarterDetails](details =>
    s"${details.serverStack}/${details.serverImplementation}/docs=${details.addDocumentation}/metrics=${details.addMetrics}" +
      s"/json=${details.jsonImplementation}/scalaVersion=${details.scalaVersion}/builder=${details.builder}"
  )

  implicit lazy val throwableShow: Show[Throwable] = Show.show[Throwable](e =>
    Using.Manager { use =>
      val sw = use(new StringWriter())
      val pw = use(new PrintWriter(sw))
      e.printStackTrace(pw)
      sw.toString
    }.get
  )
