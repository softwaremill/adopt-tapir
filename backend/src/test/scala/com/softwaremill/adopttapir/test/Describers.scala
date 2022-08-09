package com.softwaremill.adopttapir.test

import com.softwaremill.adopttapir.starter.StarterDetails

import java.io.{PrintWriter, StringWriter}
import scala.util.Using

object Describers {
  implicit class StarterDetailsWithDescribe(details: StarterDetails) {
    lazy val describe: String =
      s"${details.serverEffect}/${details.serverImplementation}/docs=${details.addDocumentation}/metrics=${details.addMetrics}" +
        s"/json=${details.jsonImplementation}/scalaVersion=${details.scalaVersion}/builder=${details.builder}"
  }

  implicit class ThrowableWithDescribe(e: Throwable) {
    def describe(): String = {
      Using.Manager { use =>
        val sw = use(new StringWriter())
        val pw = use(new PrintWriter(sw))
        e.printStackTrace(pw)
        sw.toString
      }.get
    }
  }
}
