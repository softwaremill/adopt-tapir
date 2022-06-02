package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.StarterDetails

object EndpointsSpecView {

  def getHelloServerStub(starterDetails: StarterDetails): Code =
    starterDetails.serverEffect match {
      case FutureEffect => HelloWorldStub.future
      case IOEffect     => HelloWorldStub.io
      case ZIOEffect    => HelloWorldStub.zio
    }

  object HelloWorldStub {
    val future: Code = Code(
      """val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
        |  .whenServerEndpoint(helloServerEndpoint)
        |  .thenRunLogic()
        |  .backend()""".stripMargin,
      List(
        Import("scala.concurrent.Future"),
        Import("scala.concurrent.ExecutionContext.Implicits.global")
      )
    )

    val io: Code = Code(
      """val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(SttpBackendStub.apply(new CatsMonadError[IO]()))
        |  .whenServerEndpoint(Endpoints.helloServerEndpoint)
        |  .thenRunLogic()
        |  .backend()""".stripMargin,
      List(
        Import("cats.effect.IO"),
        Import("sttp.tapir.integ.cats.CatsMonadError")
      )
    )

    val zio: Code = Code(
      """val backendStub =
        |  TapirStubInterpreter(SttpBackendStub.apply(new RIOMonadError[Any]))
        |    .whenServerEndpoint(Endpoints.helloServerEndpoint)
        |    .thenRunLogic()
        |    .backend()""".stripMargin,
      List(
        Import("sttp.tapir.ztapir.RIOMonadError")
      )
    )
  }
}
