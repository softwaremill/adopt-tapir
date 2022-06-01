package com.softwaremill.adopttapir.template.sbt

import com.softwaremill.adopttapir.starter.ServerEffect._
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.template.sbt.Dependency.{JavaDependency, ScalaDependency, ScalaTestDependency}

object BuildSbtView {

  def format(dependencies: List[Dependency]): String = {
    val space = " " * 6

    dependencies
      .map(_.asSbtDependency)
      .mkString(space, "," + System.lineSeparator() + space, "")

  }

  def getDependencies(starterDetails: StarterDetails): List[Dependency] = {
    val httpDependencies = BuildSbtView.getHttpDependencies(starterDetails)
    val monitoringDependencies = Nil
    val jsonDependencies = Nil
    val docsDepenedencies =
      if (starterDetails.addDocumentation)
        List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-bundle", Dependency.constantTapirVersion))
      else Nil
    val baseDependencies = List(
      ScalaDependency("com.typesafe.scala-logging", "scala-logging", "3.9.4"),
      JavaDependency("ch.qos.logback", "logback-classic", "1.2.11")
    )
    val testDependencies = List(
      ScalaTestDependency("com.softwaremill.sttp.tapir", "tapir-sttp-stub-server", Dependency.constantTapirVersion),
      ScalaTestDependency("org.scalatest", "scalatest", "3.2.12")
    )

    httpDependencies ++ docsDepenedencies ++ monitoringDependencies ++ jsonDependencies ++ baseDependencies ++ testDependencies
  }

  private def getHttpDependencies(starterDetails: StarterDetails): List[Dependency] = {
    starterDetails match {
      case StarterDetails(_, _, FutureEffect, Akka, _, _)  => HttpDependencies.akka()
      case StarterDetails(_, _, FutureEffect, Netty, _, _) => HttpDependencies.netty()
      case StarterDetails(_, _, IOEffect, Http4s, _, _)    => HttpDependencies.http4s()
      case StarterDetails(_, _, IOEffect, Netty, _, _)     => HttpDependencies.ioNetty()
      case StarterDetails(_, _, ZIOEffect, Http4s, _, _)   => HttpDependencies.http4sZIO()
      case StarterDetails(_, _, ZIOEffect, ZIOHttp, _, _)  => HttpDependencies.ZIOHttp()
      case other: StarterDetails => throw new UnsupportedOperationException(s"Cannot pick dependencies for $other")
    }
  }

  object HttpDependencies {
    def akka(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-akka-http-server", Dependency.constantTapirVersion)
    )

    def netty(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server", Dependency.constantTapirVersion)
    )

    def ioNetty(): List[ScalaDependency] =
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats", Dependency.constantTapirVersion) :: netty()

    def http4s(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", Dependency.constantTapirVersion),
      ScalaDependency("org.http4s", "http4s-blaze-server", "0.23.11")
    )

    def http4sZIO(): List[ScalaDependency] =
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http4s-server", Dependency.constantTapirVersion) :: http4s()

    def ZIOHttp(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http-server", Dependency.constantTapirVersion)
    )
  }
}
