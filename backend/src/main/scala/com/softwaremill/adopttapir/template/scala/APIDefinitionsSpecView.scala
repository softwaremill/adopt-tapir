package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.StarterDetails

object APIDefinitionsSpecView {

  def getHelloServerStub(starterDetails: StarterDetails): PlainLogicWithImports =
    starterDetails.serverEffect match {
      case FutureEffect => HelloWorldStub.future
      case IOEffect     => HelloWorldStub.io
      case ZIOEffect    => HelloWorldStub.zio
    }

  object HelloWorldStub {
    val future: PlainLogicWithImports = PlainLogicWithImports(
      """val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
        |  .whenServerEndpoint(helloServerEndpoint)
        |  .thenRunLogic()
        |  .backend()""".stripMargin,
      List(
        Import("scala.concurrent.Future"),
        Import("scala.concurrent.ExecutionContext.Implicits.global")
      )
    )

    val io = PlainLogicWithImports(
      """val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(SttpBackendStub.apply(new CatsMonadError[IO]()))
        |  .whenServerEndpoint(ApiDefinitions.helloServerEndpoint)
        |  .thenRunLogic()
        |  .backend()""".stripMargin,
      List(
        Import("cats.effect.IO"),
        Import("sttp.tapir.integ.cats.CatsMonadError")
      )
    )

    val zio = PlainLogicWithImports(
      """val backendStub =
        |  TapirStubInterpreter(SttpBackendStub.apply(new RIOMonadError[Any]))
        |    .whenServerEndpoint(ApiDefinitions.helloServerEndpoint)
        |    .thenRunLogic()
        |    .backend()""".stripMargin,
      List(
        Import("sttp.tapir.ztapir.RIOMonadError")
      )
    )
  }
}
