package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.*
import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.ServerImplementation.{Http4s, Netty, Pekko, VertX, ZIOHttp}
import com.softwaremill.adopttapir.template.Dependency.{JavaDependency, ScalaDependency, ScalaTestDependency, constantTapirVersion}
import com.softwaremill.adopttapir.version.TemplateDependencyInfo

abstract class BuildView:
  def getAllDependencies(starterDetails: StarterDetails): List[Dependency] =
    getMainDependencies(starterDetails) ++ getAllTestDependencies(starterDetails)

  def getMainDependencies(starterDetails: StarterDetails): List[Dependency] =
    val httpDependencies = getHttpDependencies(starterDetails)
    val monitoringDependencies = Nil
    val jsonDependencies = getJsonDependencies(starterDetails)
    val docsDependencies = getDocsDependencies(starterDetails)
    val metricsDependencies = getMetricsDependencies(starterDetails)
    val loggerDependencies = getLoggerDependency(starterDetails)

    httpDependencies ++ metricsDependencies ++ docsDependencies ++ monitoringDependencies ++ jsonDependencies ++ loggerDependencies

  def getAllTestDependencies(starterDetails: StarterDetails): List[Dependency] =
    ScalaTestDependency("com.softwaremill.sttp.tapir", "tapir-sttp-stub-server", getTapirVersion()) ::
      getTestDependencies(starterDetails.serverEffect) ++ getJsonTestDependencies(starterDetails)

  protected def getTapirVersion(): String

  private def getTestDependencies(effect: ServerEffect): List[ScalaTestDependency] =
    if effect != ServerEffect.ZIOEffect then
      List(ScalaTestDependency("org.scalatest", "scalatest", TemplateDependencyInfo.scalaTestVersion))
    else
      List(
        ScalaTestDependency("dev.zio", "zio-test", TemplateDependencyInfo.zioTestVersion),
        ScalaTestDependency("dev.zio", "zio-test-sbt", TemplateDependencyInfo.zioTestVersion)
      )

  private def getDocsDependencies(starterDetails: StarterDetails): List[ScalaDependency] =
    if starterDetails.addDocumentation then
      List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-bundle", getTapirVersion()))
    else Nil

  private def getMetricsDependencies(starterDetails: StarterDetails): List[ScalaDependency] =
    if starterDetails.addMetrics then List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-prometheus-metrics", getTapirVersion()))
    else Nil

  private def getLoggerDependency(starterDetails: StarterDetails): List[Dependency] =
    val zioLoggingDependencies =
      List(
        ScalaDependency("dev.zio", "zio-logging", "2.1.12"),
        ScalaDependency("dev.zio", "zio-logging-slf4j", "2.0.0")
      )

    val logbackClassic = List(JavaDependency("ch.qos.logback", "logback-classic", TemplateDependencyInfo.logbackClassicVersion))

    starterDetails.serverImplementation match
      case ServerImplementation.Netty   => Nil
      case ServerImplementation.ZIOHttp => logbackClassic ++ zioLoggingDependencies
      case ServerImplementation.Http4s  => logbackClassic
      case ServerImplementation.VertX   => logbackClassic
      case ServerImplementation.Pekko   => logbackClassic

  private def getJsonDependencies(starterDetails: StarterDetails): List[ScalaDependency] =
    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Nil
      case JsonImplementation.Circe =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-circe", getTapirVersion())
        )
      case JsonImplementation.UPickle =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-upickle", getTapirVersion())
        )
      case JsonImplementation.Jsoniter =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-jsoniter-scala", getTapirVersion()),
          ScalaDependency(
            "com.github.plokhotnyuk.jsoniter-scala",
            "jsoniter-scala-core",
            TemplateDependencyInfo.plokhotnyukJsoniterVersion
          ),
          ScalaDependency(
            "com.github.plokhotnyuk.jsoniter-scala",
            "jsoniter-scala-macros",
            TemplateDependencyInfo.plokhotnyukJsoniterVersion
          )
        )
      case JsonImplementation.ZIOJson =>
        List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-zio", getTapirVersion())
        )
    }

  private def getJsonTestDependencies(starterDetails: StarterDetails): List[ScalaTestDependency] =
    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Nil
      case JsonImplementation.Circe =>
        List(ScalaTestDependency("com.softwaremill.sttp.client3", "circe", TemplateDependencyInfo.sttpVersion))
      case JsonImplementation.UPickle =>
        List(ScalaTestDependency("com.softwaremill.sttp.client3", "upickle", TemplateDependencyInfo.sttpVersion))
      case JsonImplementation.Jsoniter =>
        List(ScalaTestDependency("com.softwaremill.sttp.client3", "jsoniter", TemplateDependencyInfo.sttpVersion))
      case JsonImplementation.ZIOJson =>
        List(ScalaTestDependency("com.softwaremill.sttp.client3", "zio-json", TemplateDependencyInfo.sttpVersion))
    }

  private def getHttpDependencies(starterDetails: StarterDetails): List[Dependency] =
    starterDetails match {
      case ServerEffectAndImplementation(FutureEffect, Netty) => HttpDependencies.netty()
      case ServerEffectAndImplementation(FutureEffect, VertX) => HttpDependencies.vertX()
      case ServerEffectAndImplementation(FutureEffect, Pekko) => HttpDependencies.pekko()
      case ServerEffectAndImplementation(IOEffect, Http4s)    => HttpDependencies.http4s()
      case ServerEffectAndImplementation(IOEffect, Netty)     => HttpDependencies.ioNetty()
      case ServerEffectAndImplementation(IOEffect, VertX)     => HttpDependencies.ioVerteX()
      case ServerEffectAndImplementation(ZIOEffect, Http4s)   => HttpDependencies.http4sZIO()
      case ServerEffectAndImplementation(ZIOEffect, ZIOHttp)  => HttpDependencies.ZIOHttp()
      case ServerEffectAndImplementation(ZIOEffect, VertX)    => HttpDependencies.ZIOVerteX()
      case ServerEffectAndImplementation(ZIOEffect, Netty)    => HttpDependencies.ZIONetty()
      case other: StarterDetails => throw new UnsupportedOperationException(s"Cannot pick dependencies for $other")
    }

  object HttpDependencies:
    def netty(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server", getTapirVersion())
    )

    def ioNetty(): List[ScalaDependency] =
      List(
        ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats-effect", getTapirVersion()),
        ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server-cats", getTapirVersion())
      )

    def http4s(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", getTapirVersion()),
      ScalaDependency("org.http4s", "http4s-ember-server", TemplateDependencyInfo.http4sEmberServerVersion)
    )

    def http4sZIO(): List[ScalaDependency] =
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server-zio", getTapirVersion()) :: http4s()

    def ZIOHttp(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http-server", getTapirVersion())
    )

    def vertX(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-vertx-server", getTapirVersion())
    )

    def ioVerteX(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-vertx-server-cats", getTapirVersion()),
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats-effect", getTapirVersion())
    )

    def ZIOVerteX(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-vertx-server-zio", getTapirVersion())
    )

    def ZIONetty(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server-zio", getTapirVersion())
    )

    def pekko(): List[ScalaDependency] = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-pekko-http-server", getTapirVersion())
    )

end BuildView

object BuildSbtView extends BuildView:
  def format(dependencies: List[Dependency]): String =
    val space = " " * 6

    dependencies
      .map(_.asSbtDependency)
      .mkString(space, "," + System.lineSeparator() + space, "")

  override protected def getTapirVersion(): String = constantTapirVersion

object BuildScalaCliView extends BuildView:
  def format(dependencies: List[Dependency], test: Boolean): String =
    val importPrefix = if test then "//> using test.dep " else "//> using dep "
    dependencies
      .map(_.asScalaCliDependency)
      .mkString(importPrefix, System.lineSeparator() + importPrefix, System.lineSeparator())

  override protected def getTapirVersion(): String = TemplateDependencyInfo.tapirVersion
