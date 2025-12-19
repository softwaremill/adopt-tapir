package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{JsonImplementation, ScalaVersion, ServerStack, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.{booksListingServerEndpoint, helloServerEndpoint}

object EndpointsSpecView:

  def getHelloServerStub(starterDetails: StarterDetails): Code =
    Stub.prepareBackendStub(helloServerEndpoint, starterDetails.serverStack)

  def getBookServerStub(starterDetails: StarterDetails): Code =

    val stubBooks = Stub
      .prepareBackendStub(booksListingServerEndpoint, starterDetails.serverStack)

    starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => Code.empty
      case JsonImplementation.Circe       =>
        stubBooks.addImports(
          Set(
            Import("io.circe.generic.auto._"),
            Import("sttp.client4.circe._"),
            Import("Library._")
          )
        )
      case JsonImplementation.UPickle => stubBooks.addImports(Set(Import("sttp.client4.upicklejson._"), Import("Library._")))
      case JsonImplementation.Pickler =>
        stubBooks.addImports(
          Set(
            Import("upickle.default.Reader"),
            Import("sttp.client4.upicklejson._"),
            Import("Library._")
          )
        )
      case JsonImplementation.Jsoniter => stubBooks.addImports(Set(Import("sttp.client4.jsoniter._"), Import("Library._")))
      case JsonImplementation.ZIOJson  => stubBooks.addImports(Set(Import("sttp.client4.ziojson._"), Import("Library._")))
    }

  object Stub:
    def prepareBackendStub(endpoint: String, serverStack: ServerStack): Code =
      val (stub, interpreter) = serverStack match {
        case ServerStack.FutureStack => ("BackendStub.asynchronousFuture", "TapirStubInterpreter")
        case ServerStack.IOStack     => ("BackendStub[IO]", "TapirStubInterpreter")
        case ServerStack.ZIOStack    => ("BackendStub[ZIO[Any, Throwable, *]]", "TapirStubInterpreter")
        case ServerStack.OxStack     => ("SyncBackendStub.synchronous", "TapirSyncStubInterpreter")
      }

      val body =
        s"""val backendStub = $interpreter($stub)
           |  .whenServerEndpointRunLogic($endpoint)
           |  .backend()""".stripMargin

      val imports = serverStack match {
        case ServerStack.FutureStack =>
          Set(
            Import("scala.concurrent.Future"),
            Import("scala.concurrent.ExecutionContext.Implicits.global"),
            Import("sttp.client4.testing.BackendStub"),
            Import("sttp.tapir.server.stub4.TapirStubInterpreter")
          )
        case ServerStack.IOStack =>
          Set(
            Import("cats.effect.IO"),
            Import("sttp.client4.testing.BackendStub"),
            Import("sttp.tapir.server.stub4.TapirStubInterpreter")
          )
        case ServerStack.ZIOStack =>
          Set(
            Import("zio.ZIO"),
            Import("sttp.client4.testing.BackendStub"),
            Import("sttp.tapir.server.stub4.TapirStubInterpreter")
          )
        case ServerStack.OxStack =>
          Set(
            Import("sttp.client4.testing.SyncBackendStub"),
            Import("sttp.tapir.server.stub4.TapirSyncStubInterpreter")
          )
      }

      Code(body, imports)

  object Unwrapper:
    def prepareUnwrapper(stack: ServerStack, scalaVersion: ScalaVersion): Code =
      def prepareBody(kind: String, unwrapFn: String): String = {
        case class ExtensionMethodVersion(prefix: String, codeBlockStart: String, codeBlockEnd: String)
        val scala2Extension = ExtensionMethodVersion("implicit class Unwrapper", " {", "}")
        val scala3Extension = ExtensionMethodVersion("extension", "", "")

        val templateFn: ExtensionMethodVersion => String = v => s"""${v.prefix}[T](t: $kind)${v.codeBlockStart}
             |   def unwrap: T = $unwrapFn
             |${v.codeBlockEnd}""".stripMargin

        scalaVersion match {
          case ScalaVersion.Scala2 => templateFn(scala2Extension)
          case ScalaVersion.Scala3 => templateFn(scala3Extension)
        }

      }

      stack match {
        case ServerStack.FutureStack =>
          Code(
            prepareBody("Future[T]", "Await.result(t, Duration.Inf)"),
            Set(
              Import("scala.concurrent.Await"),
              Import("scala.concurrent.Future"),
              Import("scala.concurrent.duration.Duration")
            )
          )
        case ServerStack.IOStack =>
          Code(prepareBody("IO[T]", "t.unsafeRunSync()"), Set(Import("cats.effect.unsafe.implicits.global")))
        case ServerStack.ZIOStack =>
          Code(prepareBody("ZIO[Any, Throwable, T]", "zio.Runtime.default.unsafeRun(t)"), Set(Import("zio.ZIO")))
        case ServerStack.OxStack =>
          throw new UnsupportedOperationException("Should not unwrap OxStack effect")
      }
