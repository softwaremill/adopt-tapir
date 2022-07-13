package com.softwaremill.adopttapir.template.sbt

import com.softwaremill.adopttapir.starter.ServerEffect._
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.sbt.Dependency._
import com.softwaremill.adopttapir.version.TemplateDependencyInfo
import com.softwaremill.adopttapir.version.TemplateDependencyInfo._

object BuildSbtView {

  def format(dependencies: List[Dependency]): String = {
    val space = " " * 6

    dependencies
      .map(_.asSbtDependency)
      .mkString(space, "," + System.lineSeparator() + space, "")

  }

  def getDependencies(starterDetails: StarterDetails): List[Dependency] = {
    val httpDependencies = getHttpDependencies(starterDetails)
    val monitoringDependencies = Nil
    val jsonDependencies = getJsonDependencies(starterDetails)
    val docsDepenedencies = getDocsDependencies(starterDetails)
    val metricsDependencies = getMetricsDependencies(starterDetails)
    val baseDependencies = List(
      ScalaDependency("com.typesafe.scala-logging", "scala-logging", scalaLoggingVersion),
      JavaDependency("ch.qos.logback", "logback-classic", logbackClassicVersion)
    )
    val testDependencies = ScalaTestDependency("com.softwaremill.sttp.tapir", "tapir-sttp-stub-server", constantTapirVersion) ::
      getTestDependencies(starterDetails.serverEffect) ++ getJsonTestDependencies(starterDetails)

    httpDependencies ++ metricsDependencies ++ docsDepenedencies ++ monitoringDependencies ++ jsonDependencies ++ baseDependencies ++ testDependencies
  }

  private def getTestDependencies(effect: ServerEffect): List[ScalaTestDependency] = {
    if (effect != ServerEffect.ZIOEffect) List(ScalaTestDependency("org.scalatest", "scalatest", TemplateDependencyInfo.scalaTestVersion))
    else
      List(
        ScalaTestDependency("dev.zio", "zio-test", TemplateDependencyInfo.zioTestVersion),
        ScalaTestDependency("dev.zio", "zio-test-sbt", TemplateDependencyInfo.zioTestVersion)
      )
  }

  private def getDocsDependencies(starterDetails: StarterDetails): List[ScalaDependency] = {
    if (starterDetails.addDocumentation)
      List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-bundle", constantTapirVersion))
    else Nil
  }

  private def getMetricsDependencies(starterDetails: StarterDetails): List[ScalaDependency] = {
    if (starterDetails.addMetrics)
      List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-prometheus-metrics", constantTapirVersion))
    else Nil
  }

  private def getJsonDependencies(starterDetails: StarterDetails): List[ScalaDependency] = {
    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Nil
      case JsonImplementation.Circe =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-circe", constantTapirVersion)
        )
      case JsonImplementation.Jsoniter =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-jsoniter-scala", constantTapirVersion),
          ScalaDependency("com.github.plokhotnyuk.jsoniter-scala", "jsoniter-scala-core", plokhotnyukJsoniterVersion),
          ScalaDependency("com.github.plokhotnyuk.jsoniter-scala", "jsoniter-scala-macros", plokhotnyukJsoniterVersion)
        )
      case JsonImplementation.ZIOJson =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-zio", constantTapirVersion)
        )
    }
  }

  private def getJsonTestDependencies(starterDetails: StarterDetails): List[ScalaTestDependency] = {
    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Nil
      case JsonImplementation.Circe       => List(ScalaTestDependency("com.softwaremill.sttp.client3", "circe", sttpVersion))
      case JsonImplementation.Jsoniter    => List(ScalaTestDependency("com.softwaremill.sttp.client3", "jsoniter", sttpVersion))
      case JsonImplementation.ZIOJson     => List(ScalaTestDependency("com.softwaremill.sttp.client3", "zio-json", sttpVersion))
    }
  }

  private def getHttpDependencies(starterDetails: StarterDetails): List[Dependency] = {
    starterDetails match {
      case StarterDetails(_, _, FutureEffect, Akka, _, _, _, _)  => HttpDependencies.akka()
      case StarterDetails(_, _, FutureEffect, Netty, _, _, _, _) => HttpDependencies.netty()
      case StarterDetails(_, _, IOEffect, Http4s, _, _, _, _)    => HttpDependencies.http4s()
      case StarterDetails(_, _, IOEffect, Netty, _, _, _, _)     => HttpDependencies.ioNetty()
      case StarterDetails(_, _, ZIOEffect, Http4s, _, _, _, _)   => HttpDependencies.http4sZIO()
      case StarterDetails(_, _, ZIOEffect, ZIOHttp, _, _, _, _)  => HttpDependencies.ZIOHttp()
      case other: StarterDetails => throw new UnsupportedOperationException(s"Cannot pick dependencies for $other")
    }
  }

  object HttpDependencies {
    def akka(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-akka-http-server", constantTapirVersion)
    )

    def netty(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server", constantTapirVersion)
    )

    def ioNetty(): List[ScalaDependency] =
      List(
        ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats", constantTapirVersion),
        ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server-cats", constantTapirVersion)
      )

    def http4s(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", constantTapirVersion),
      ScalaDependency("org.http4s", "http4s-blaze-server", TemplateDependencyInfo.http4sVersion)
    )

    def http4sZIO(): List[ScalaDependency] =
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server-zio", constantTapirVersion) :: http4s()

    def ZIOHttp(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http-server", constantTapirVersion)
    )
  }
}
