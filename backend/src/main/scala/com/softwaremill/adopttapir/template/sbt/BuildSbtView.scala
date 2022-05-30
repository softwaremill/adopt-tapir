package com.softwaremill.adopttapir.template.sbt

import com.softwaremill.adopttapir.starter.ServerEffect._
import com.softwaremill.adopttapir.starter.ServerImplementation.{Akka, Http4s, Netty, ZIOHttp}
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.template.sbt.Dependency.ScalaDependency

object BuildSbtView {

  def getHttpDependencies(starterDetails: StarterDetails): List[Dependency] = {
    starterDetails match {
      case StarterDetails(_, _, FutureEffect, Akka, tapirVersion)  => HttpDependencies.akka(tapirVersion)
      case StarterDetails(_, _, FutureEffect, Netty, tapirVersion) => HttpDependencies.netty(tapirVersion)
      case StarterDetails(_, _, IOEffect, Http4s, tapirVersion)    => HttpDependencies.http4s(tapirVersion)
      case StarterDetails(_, _, IOEffect, Netty, tapirVersion)     => HttpDependencies.netty(tapirVersion)
      case StarterDetails(_, _, ZIOEffect, Http4s, tapirVersion)   => HttpDependencies.http4sZIO(tapirVersion)
      case StarterDetails(_, _, ZIOEffect, ZIOHttp, tapirVersion)  => HttpDependencies.ZIOHttp(tapirVersion)
      case other: StarterDetails => throw new UnsupportedOperationException(s"Cannot pick dependencies for $other")
    }
  }

  object HttpDependencies {
    def akka(tapirVersion: String) = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-akka-http-server", tapirVersion),
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats", tapirVersion)
    )
    def netty(tapirVersion: String) = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server", tapirVersion),
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-cats", tapirVersion)
    )

    def http4s(tapirVersion: String) = List(
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", tapirVersion),
      ScalaDependency("org.http4s", "http4s-blaze-server", "0.23.11")
    )

    def http4sZIO(tapirVersion: String) =
      ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http4s-server", tapirVersion) :: http4s(tapirVersion)

    def ZIOHttp(tapirVersion: String) = List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http-server", tapirVersion))
  }
}
