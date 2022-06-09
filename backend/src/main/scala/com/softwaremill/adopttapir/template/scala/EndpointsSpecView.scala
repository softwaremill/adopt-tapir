package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ServerEffect.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.{ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.helloServerEndpoint

object EndpointsSpecView {

  def getHelloServerStub(starterDetails: StarterDetails): Code =
    starterDetails.serverEffect match {
      case FutureEffect => HelloWorldStub.future
      case IOEffect     => HelloWorldStub.io
      case ZIOEffect    => HelloWorldStub.zio
    }

  object HelloWorldStub {
    val future: Code = Code(
      s"""val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
         |  .whenServerEndpoint($helloServerEndpoint)
         |  .thenRunLogic()
         |  .backend()""".stripMargin,
      List(
        Import("scala.concurrent.Future"),
        Import("scala.concurrent.ExecutionContext.Implicits.global")
      )
    )

    val io: Code = Code(
      s"""val backendStub: SttpBackend[IO, Any] = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
         |  .whenServerEndpoint(Endpoints.$helloServerEndpoint)
         |  .thenRunLogic()
         |  .backend()""".stripMargin,
      List(
        Import("cats.effect.IO"),
        Import("sttp.tapir.integ.cats.CatsMonadError")
      )
    )

    val zio: Code = Code(
      s"""val backendStub =
         |  TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
         |    .whenServerEndpoint(Endpoints.$helloServerEndpoint)
         |    .thenRunLogic()
         |    .backend()""".stripMargin,
      List(
        Import("sttp.tapir.ztapir.RIOMonadError")
      )
    )
  }

  object Rich {
    def prepareUnwrapper(effect: ServerEffect): Code = {
      def prepareCode(kind: String, unwrapFn: String) =
        s"""implicit class Unwrapper[T](t: $kind) {
           |   def unwrap: T = $unwrapFn
           |}
           |
           |""".stripMargin

      effect match {
        case ServerEffect.FutureEffect =>
          Code(
            prepareCode("Future[T]", "Await.result(t, Duration.Inf)"),
            List(
              Import("scala.concurrent.{Await, Future}"),
              Import("scala.concurrent.duration.Duration")
            )
          )
        case ServerEffect.IOEffect =>
          Code(prepareCode("IO[T]", "t.unsafeRunSync()"), List(Import("cats.effect.unsafe.implicits.global")))
        case ServerEffect.ZIOEffect =>
          Code(prepareCode("ZIO[Any, Throwable, T]", "zio.Runtime.default.unsafeRun(t)"), List(Import("zio.ZIO")))
      }

    }
  }
}
