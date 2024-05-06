package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.ScalaVersion.Scala2
import com.softwaremill.adopttapir.starter.{JsonImplementation, ScalaVersion, ServerStack, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.*

object EndpointsView:

  def getHelloServerEndpoint(starterDetails: StarterDetails): Code =
    val helloServerCode = starterDetails.serverStack match {
      case ServerStack.FutureStack => HelloServerEndpoint.future
      case ServerStack.IOStack     => HelloServerEndpoint.io
      case ServerStack.ZIOStack    => HelloServerEndpoint.zio
      case ServerStack.OxStack     => HelloServerEndpoint.sync
    }

    helloServerCode.prependBody(INDENT)

  private object HelloServerEndpoint:
    def bodyTemplate(serverKind: String, pureEffectFn: String): String =
      s"""${INDENT}val $helloServerEndpoint: $serverKind = $helloEndpoint.serverLogicSuccess(user =>
         |  $pureEffectFn(s"Hello $${user.name}")
         |)""".stripMargin

    val future: Code = Code(
      bodyTemplate("ServerEndpoint[Any, Future]", "Future.successful"),
      Set(
        Import("scala.concurrent.Future"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val sync: Code = Code(
      s"""${INDENT}val $helloServerEndpoint: ServerEndpoint[Any, Id] = $helloEndpoint.serverLogicSuccess(user =>
         |  s"Hello $${user.name}"
         |)""".stripMargin,
      Set(
        Import("sttp.tapir.server.ServerEndpoint"),
        Import("sttp.tapir.server.netty.sync.Id")
      )
    )

    val io: Code = Code(
      bodyTemplate("ServerEndpoint[Any, IO]", "IO.pure"),
      Set(
        Import("cats.effect.IO"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val zio: Code = Code(
      bodyTemplate("ZServerEndpoint[Any, Any]", "ZIO.succeed"),
      Set(
        Import("zio.ZIO"),
        Import("sttp.tapir.ztapir.ZServerEndpoint")
      )
    )

  def getJsonOutModel(starterDetails: StarterDetails): Code =
    JsonModelObject.prepareJsonEndpoint(starterDetails)

  private object JsonModelObject:

    def prepareJsonEndpoint(starterDetails: StarterDetails): Code =
      starterDetails.jsonImplementation match {
        case JsonImplementation.WithoutJson => Code.empty
        case _ =>
          List(prepareBookListing(starterDetails), prepareBookListingServerLogic(starterDetails))
            .reduce((a, b) => Code(a.body + NEW_LINE_WITH_INDENT + b.body, a.imports ++ b.imports))
            .prependBody(INDENT)
      }

    def prepareLibraryModel(starterDetails: StarterDetails): Code = {
      val objects =
        s"""object Library ${if starterDetails.scalaVersion == Scala2 then "{" else ":"}
         |  case class Author(name: String)
         |  case class Book(title: String, year: Int, author: Author)
         |
         |""".stripMargin

      val implicits = starterDetails.jsonImplementation match {
        case JsonImplementation.UPickle =>
          s"""
           |  object Author ${if starterDetails.scalaVersion == Scala2 then "{" else ":"}
           |    implicit val rw: ReadWriter[Author] = macroRW
           |  ${if starterDetails.scalaVersion == Scala2 then "}" else ""}
           |
           |  object Book ${if starterDetails.scalaVersion == Scala2 then "{" else ":"}
           |    implicit val rw: ReadWriter[Book] = macroRW
           |  ${if starterDetails.scalaVersion == Scala2 then "}" else ""}
           |
           |""".stripMargin
        case JsonImplementation.Pickler =>
          s"""
               |  given Pickler[Author] = Pickler.derived
               |  given Pickler[Book] = Pickler.derived
               |
               |""".stripMargin
        case _ => ""
      }

      val list =
        s"""|  val books = List(
            |    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
            |    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
            |    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
            |    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
            |  )
            |${if starterDetails.scalaVersion == Scala2 then "}" else ""}""".stripMargin

      Code(
        objects + implicits + list,
        Set(
          Import("Library._")
        )
      )
    }

    private def prepareBookListing(starterDetails: StarterDetails): Code =
      val givenPrefix = if starterDetails.scalaVersion == Scala2 then "implicit val" else "given"

      def prepareBookListing: String =
        s"""val $bookListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
           |  .in("books" / "list" / "all")
           |  .out(jsonBody[List[Book]])""".stripMargin

      starterDetails.jsonImplementation match {
        case JsonImplementation.WithoutJson => Code.empty
        case JsonImplementation.Circe =>
          Code(
            prepareBookListing,
            Set(
              Import("io.circe.generic.auto._"),
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.circe._")
            )
          )
        case JsonImplementation.UPickle =>
          Code(
            prepareBookListing,
            Set(Import("sttp.tapir.generic.auto._"), Import("upickle.default._"), Import("sttp.tapir.json.upickle._"))
          )
        case JsonImplementation.Pickler => Code(prepareBookListing, Set(Import("sttp.tapir.json.pickler._")))
        case JsonImplementation.Jsoniter =>
          val codecs = s"$givenPrefix codecBooks: JsonValueCodec[List[Book]] = JsonCodecMaker.make"

          Code(
            codecs + NEW_LINE_WITH_INDENT + prepareBookListing,
            Set(
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.jsoniter._"),
              Import("com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec"),
              Import("com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker")
            )
          )
        case JsonImplementation.ZIOJson =>
          val codecs = {
            s"$givenPrefix authorZioEncoder: zio.json.JsonEncoder[Author] = DeriveJsonEncoder.gen[Author]" + NEW_LINE_WITH_INDENT +
              s"$givenPrefix authorZioDecoder: zio.json.JsonDecoder[Author] = DeriveJsonDecoder.gen[Author]" + NEW_LINE_WITH_INDENT +
              s"$givenPrefix bookZioEncoder: zio.json.JsonEncoder[Book] = DeriveJsonEncoder.gen[Book]" + NEW_LINE_WITH_INDENT +
              s"$givenPrefix bookZioDecoder: zio.json.JsonDecoder[Book] = DeriveJsonDecoder.gen[Book]"
          }

          Code(
            codecs + NEW_LINE_WITH_INDENT + prepareBookListing,
            Set(
              Import("sttp.tapir.Codec.JsonCodec"),
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.zio._"),
              Import("zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}")
            )
          )
      }

    private def prepareBookListingServerLogic(starterDetails: StarterDetails): Code =
      val (serverKind, pureEffectFn) = starterDetails.serverStack match {
        case ServerStack.FutureStack => ("ServerEndpoint[Any, Future]", "Future.successful")
        case ServerStack.OxStack     => ("ServerEndpoint[Any, Id]", "")
        case ServerStack.IOStack     => ("ServerEndpoint[Any, IO]", "IO.pure")
        case ServerStack.ZIOStack    => ("ZServerEndpoint[Any, Any]", "ZIO.succeed")
      }

      // Imports silently taken from helloServerEndpoint
      Code(s"val $booksListingServerEndpoint: $serverKind = $bookListing.serverLogicSuccess(_ => $pureEffectFn(Library.books))")

  def getJsonLibrary(starterDetails: StarterDetails): Code =
    if starterDetails.jsonImplementation == JsonImplementation.WithoutJson then Code.empty
    else JsonModelObject.prepareLibraryModel(starterDetails)

  def getApiEndpoints(starterDetails: StarterDetails): Code =
    val serverKind = starterDetails.serverStack match {
      case ServerStack.FutureStack => "List[ServerEndpoint[Any, Future]]"
      case ServerStack.OxStack     => "List[ServerEndpoint[Any, Id]]"
      case ServerStack.IOStack     => "List[ServerEndpoint[Any, IO]]"
      case ServerStack.ZIOStack    => "List[ZServerEndpoint[Any, Any]]"
    }

    val jsonEndpoint = if starterDetails.jsonImplementation == JsonImplementation.WithoutJson then Nil else List(booksListingServerEndpoint)
    val endpoints = List(helloServerEndpoint) ++ jsonEndpoint
    Code(
      s"val $apiEndpoints: $serverKind = List(${endpoints.mkString(",")})"
    ).prependBody(INDENT)

  def getDocEndpoints(starterDetails: StarterDetails): Code =
    if starterDetails.addDocumentation then {
      DocumentationEndpoint.prepareDocEndpoints(
        starterDetails.projectName,
        starterDetails.serverStack
      )
    } else Code.empty

  private object DocumentationEndpoint:

    def prepareDocEndpoints(
        projectName: String,
        serverStack: ServerStack
    ): Code =
      Code(prepareCode(projectName, serverStack), prepareImports(serverStack)).prependBody(INDENT)

    private def prepareCode(projectName: String, serverStack: ServerStack): String =
      val (effect, endpoint) = serverStackToEffectAndEndpoint(serverStack)
      s"""val $docEndpoints: List[$endpoint] = SwaggerInterpreter()
          .fromServerEndpoints[$effect]($apiEndpoints, "$projectName", "1.0.0")""".stripMargin

    def prepareImports(serverStack: ServerStack): Set[Import] =
      serverStackImports(serverStack) + Import("sttp.tapir.swagger.bundle.SwaggerInterpreter")

  def getMetricsEndpoint(starterDetails: StarterDetails): Code =
    if starterDetails.addMetrics then {
      MetricsEndpoint.prepareMetricsEndpoint(starterDetails.serverStack)
    } else {
      Code.empty
    }

  private object MetricsEndpoint:
    def prepareMetricsEndpoint(serverStack: ServerStack): Code =
      val (effect, endpoint) = serverStackToEffectAndEndpoint(serverStack)
      Code(
        s"${INDENT}val prometheusMetrics: PrometheusMetrics[$effect] = PrometheusMetrics.default[$effect]()" + NEW_LINE_WITH_INDENT +
          s"val metricsEndpoint: $endpoint = prometheusMetrics.metricsEndpoint",
        serverStackImports(serverStack) + Import("sttp.tapir.server.metrics.prometheus.PrometheusMetrics")
      )

  private def serverStackToEffectAndEndpoint(serverStack: ServerStack): (String, String) =
    serverStack match {
      case ServerStack.FutureStack => ("Future", "ServerEndpoint[Any, Future]")
      case ServerStack.OxStack     => ("Id", "ServerEndpoint[Any, Id]")
      case ServerStack.IOStack     => ("IO", "ServerEndpoint[Any, IO]")
      case ServerStack.ZIOStack    => ("Task", "ZServerEndpoint[Any, Any]")
    }

  private def serverStackImports(serverStack: ServerStack): Set[Import] =
    serverStack match {
      case ServerStack.ZIOStack => Set(Import("zio.Task"))
      case _                    => Set.empty[Import]
    }

  def getAllEndpoints(starterDetails: StarterDetails): Code =
    val serverKind = starterDetails.serverStack match {
      case ServerStack.FutureStack => "List[ServerEndpoint[Any, Future]]"
      case ServerStack.OxStack     => "List[ServerEndpoint[Any, Id]]"
      case ServerStack.IOStack     => "List[ServerEndpoint[Any, IO]]"
      case ServerStack.ZIOStack    => "List[ZServerEndpoint[Any, Any]]"
    }

    def bodyTemplate(serverKind: String, addMetrics: Boolean, hasDocumentation: Boolean): String = {
      s"val ${Constants.all}: $serverKind = $apiEndpoints" +
        s"${if hasDocumentation then s" ++ $docEndpoints" else ""}" +
        s"${if addMetrics then s" ++ List($metricsEndpoint)" else ""}"

    }

    Code(bodyTemplate(serverKind, starterDetails.addMetrics, starterDetails.addDocumentation)).prependBody(INDENT)

  object Constants:
    val INDENT: String = " " * 2
    val NEW_LINE_WITH_INDENT: String = System.lineSeparator() + INDENT
    val helloEndpoint = "helloEndpoint"
    val helloServerEndpoint = "helloServerEndpoint"
    val bookListing = "booksListing"
    val booksListingServerEndpoint = "booksListingServerEndpoint"
    val apiEndpoints = "apiEndpoints"
    val docEndpoints = "docEndpoints"
    val metricsEndpoint = "metricsEndpoint"
    val all = "all"
