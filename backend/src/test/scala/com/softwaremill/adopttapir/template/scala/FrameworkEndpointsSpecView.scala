package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsSpecView.Stub
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.metricsEndpoint

object FrameworkEndpointsSpecView {
  def getMetricsServerStub(starterDetails: StarterDetails): Code = {
    if (starterDetails.addMetrics) Stub.prepareBackendStub(metricsEndpoint, starterDetails.serverEffect) else Code.empty
  }

  def getDocsServerStub(starterDetails: StarterDetails): Code = {
    if (starterDetails.addDocumentation) prepareDocsStub(starterDetails.serverEffect) else Code.empty
  }

  private def prepareDocsStub(serverEffect: ServerEffect): Code = {
    val stub = serverEffect match {
      case ServerEffect.FutureEffect => "SttpBackendStub.asynchronousFuture"
      case ServerEffect.IOEffect     => "SttpBackendStub(new CatsMonadError[IO]())"
      case ServerEffect.ZIOEffect    => "SttpBackendStub(new RIOMonadError[Any])"
    }

    val body =
      s"""val backendStub = TapirStubInterpreter($stub)
         |  .whenServerEndpointsRunLogic(docEndpoints)
         |  .backend()""".stripMargin

    val imports = serverEffect match {
      case ServerEffect.FutureEffect =>
        Set(
          Import("scala.concurrent.Future"),
          Import("scala.concurrent.ExecutionContext.Implicits.global")
        )
      case ServerEffect.IOEffect =>
        Set(
          Import("cats.effect.IO"),
          Import("sttp.tapir.integ.cats.CatsMonadError")
        )
      case ServerEffect.ZIOEffect =>
        Set(
          Import("sttp.tapir.ztapir.RIOMonadError")
        )
    }

    Code(body, imports)
  }
}
