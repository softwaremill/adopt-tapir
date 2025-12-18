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
            Import("sttp.client3.circe._"),
            Import("Library._")
          )
        )
      case JsonImplementation.UPickle => stubBooks.addImports(Set(Import("sttp.client3.upicklejson._"), Import("Library._")))
      case JsonImplementation.Pickler =>
        stubBooks.addImports(
          Set(
            Import("upickle.default.Reader"),
            Import("sttp.client3.upicklejson._"),
            Import("Library._")
          )
        )
      case JsonImplementation.Jsoniter => stubBooks.addImports(Set(Import("sttp.client3.jsoniter._"), Import("Library._")))
      case JsonImplementation.ZIOJson  => stubBooks.addImports(Set(Import("sttp.client3.ziojson._"), Import("Library._")))
    }

  object Stub:
    def prepareBackendStub(endpoint: String, serverStack: ServerStack): Code =
      val stub = serverStack match {
        case ServerStack.FutureStack => "SttpBackendStub.asynchronousFuture"
        case ServerStack.IOStack     => "SttpBackendStub(new CatsMonadError[IO]())"
        case ServerStack.ZIOStack    => "SttpBackendStub(new RIOMonadError[Any])"
        case ServerStack.OxStack     => "SttpBackendStub.synchronous"
      }

      val body =
        s"""val backendStub = TapirStubInterpreter($stub)
           |  .whenServerEndpointRunLogic($endpoint)
           |  .backend()""".stripMargin

      val imports = serverStack match {
        case ServerStack.FutureStack =>
          Set(
            Import("scala.concurrent.Future"),
            Import("scala.concurrent.ExecutionContext.Implicits.global")
          )
        case ServerStack.IOStack =>
          Set(
            Import("cats.effect.IO"),
            Import("sttp.tapir.integ.cats.effect.CatsMonadError")
          )
        case ServerStack.ZIOStack =>
          Set(
            Import("sttp.tapir.ztapir.RIOMonadError")
          )
        case ServerStack.OxStack =>
          Set.empty
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
