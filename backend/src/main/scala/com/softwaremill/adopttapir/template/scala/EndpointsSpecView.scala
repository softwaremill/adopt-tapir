package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.{booksListingServerEndpoint, helloServerEndpoint}

object EndpointsSpecView {

  def getHelloServerStub(starterDetails: StarterDetails): Code = {
    Stub.prepareBackendStub(helloServerEndpoint, starterDetails.serverEffect)
  }

  def getBookServerStub(starterDetails: StarterDetails): Code = {

    val stubBooks = Stub
      .prepareBackendStub(booksListingServerEndpoint, starterDetails.serverEffect)

    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Code.empty
      case JsonImplementation.Circe =>
        stubBooks.addImports(
          Set(
            Import("io.circe.generic.auto._"),
            Import("sttp.client3.circe._")
          )
        )
      case JsonImplementation.Jsoniter => stubBooks.addImport(Import("sttp.client3.jsoniter._"))
      case JsonImplementation.ZIOJson  => stubBooks.addImport(Import("sttp.client3.ziojson._"))
    }

  }

  private object Stub {
    def prepareBackendStub(endpoint: String, serverEffect: ServerEffect): Code = {
      val stub = serverEffect match {
        case ServerEffect.FutureEffect => "SttpBackendStub.asynchronousFuture"
        case ServerEffect.IOEffect     => "SttpBackendStub(new CatsMonadError[IO]())"
        case ServerEffect.ZIOEffect    => "SttpBackendStub(new RIOMonadError[Any])"
      }

      val body =
        s"""val backendStub = TapirStubInterpreter($stub)
           |  .whenServerEndpoint($endpoint)
           |  .thenRunLogic()
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

  object Unwrapper {
    def prepareUnwrapper(effect: ServerEffect): Code = {
      def prepareBody(kind: String, unwrapFn: String): String =
        s"""implicit class Unwrapper[T](t: $kind) {
           |   def unwrap: T = $unwrapFn
           |}""".stripMargin

      effect match {
        case ServerEffect.FutureEffect =>
          Code(
            prepareBody("Future[T]", "Await.result(t, Duration.Inf)"),
            Set(
              Import("scala.concurrent.Await"),
              Import("scala.concurrent.Future"),
              Import("scala.concurrent.duration.Duration")
            )
          )
        case ServerEffect.IOEffect =>
          Code(prepareBody("IO[T]", "t.unsafeRunSync()"), Set(Import("cats.effect.unsafe.implicits.global")))
        case ServerEffect.ZIOEffect =>
          Code(prepareBody("ZIO[Any, Throwable, T]", "zio.Runtime.default.unsafeRun(t)"), Set(Import("zio.ZIO")))
      }
    }
  }
}
