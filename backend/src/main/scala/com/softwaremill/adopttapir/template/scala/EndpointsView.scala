package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants._

object EndpointsView {

  def getHelloServerEndpoint(starterDetails: StarterDetails): Code = starterDetails.serverEffect match {
    case ServerEffect.FutureEffect => HelloServerEndpoint.future
    case ServerEffect.IOEffect     => HelloServerEndpoint.io
    case ServerEffect.ZIOEffect    => HelloServerEndpoint.zio
  }

  def getMetricsEndpoint(starterDetails: StarterDetails): Code =
    if (starterDetails.addMetrics) {
      MetricsEndpoint.prepareMetricsEndpoint(starterDetails.serverEffect)
    } else {
      Code.empty
    }

  def getDocEndpoints(starterDetails: StarterDetails): Code = {
    if (starterDetails.addDocumentation) {
      DocumentationEndpoint.prepareDocEndpoints(starterDetails.projectName, starterDetails.serverEffect, starterDetails.addMetrics, starterDetails.jsonImplementation)
    } else
      Code.empty
  }

  def getJsonOutModel(starterDetails: StarterDetails): Code = {
    JsonModelObject.prepareJsonEndpoint(starterDetails)
  }

  def getJsonLibrary(starterDetails: StarterDetails): Code = {
    if (starterDetails.jsonImplementation == JsonImplementation.WithoutJson) Code.empty else JsonModelObject.prepareLibraryModel()
  }

  def getAllEndpoints(starterDetails: StarterDetails): Code = {
    val serverKind = starterDetails.serverEffect match {
      case ServerEffect.FutureEffect => "List[ServerEndpoint[Any, Future]]"
      case ServerEffect.IOEffect     => "List[ServerEndpoint[Any, IO]]"
      case ServerEffect.ZIOEffect    => "List[ZServerEndpoint[Any, Any]]"
    }

    val hasBooksListingEndpoint = starterDetails.jsonImplementation match {
      case JsonImplementation.WithoutJson => false
      case _                              => true
    }

    def bodyTemplate(serverKind: String, hasJson: Boolean, addMetrics: Boolean, hasDocumentation: Boolean): String = {
      s"val ${Constants.all}: $serverKind = List($helloServerEndpoint" +
        s"${if (hasJson) s", $booksListingServerEndpoint" else ""}" +
        s"${if (addMetrics) s", $metricsEndpoint" else ""}" +
        ")" +
        s"${if (hasDocumentation)s" ++ $docEndpoints"else ""}"
    }

    Code(bodyTemplate(serverKind, hasBooksListingEndpoint, starterDetails.addMetrics, starterDetails.addDocumentation))
  }

  private object HelloServerEndpoint {
    def bodyTemplate(serverKind: String, pureEffectFn: String): String =
      s"""  val $helloServerEndpoint: $serverKind = $helloEndpoint.serverLogicSuccess(user =>
         |    $pureEffectFn(s"Hello $${user.name}")
         |  )""".stripMargin

    val future: Code = Code(
      bodyTemplate("ServerEndpoint[Any, Future]", "Future.successful"),
      Set(
        Import("scala.concurrent.Future"),
        Import("sttp.tapir._"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val io: Code = Code(
      bodyTemplate("ServerEndpoint[Any, IO]", "IO.pure"),
      Set(
        Import("cats.effect.IO"),
        Import("sttp.tapir._"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val zio: Code = Code(
      bodyTemplate("ZServerEndpoint[Any, Any]", "ZIO.succeed"),
      Set(
        Import("sttp.tapir.ztapir._"),
        Import("zio.ZIO")
      )
    )
  }

  private object JsonModelObject {

    def prepareJsonEndpoint(starterDetails: StarterDetails): Code = {
      starterDetails.jsonImplementation match {
        case JsonImplementation.WithoutJson => Code.empty
        case _ =>
          List(prepareBookListing(starterDetails), prepareBookListingServerLogic(starterDetails))
            .reduce((a, b) => Code(a.body + System.lineSeparator() + b.body, a.imports ++ b.imports))
      }
    }

    def prepareLibraryModel(): Code = Code(
      """object Library {
        |  case class Author(name: String)
        |  case class Book(title: String, year: Int, author: Author)
        |
        |  val books = new AtomicReference(
        |    List(
        |      Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
        |      Book("Nad Niemnem", 1888, Author("Eliza Orzeszkowa")),
        |      Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
        |      Book("Pharaoh", 1897, Author("Boleslaw Prus"))
        |    )
        |  )
        |}""".stripMargin,
      Set(
        Import("java.util.concurrent.atomic.AtomicReference"),
        Import("Library._")
      )
    )

    private def prepareBookListing(starterDetails: StarterDetails): Code = {
      def prepareBookListing: String = {
        s"""  val $bookListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
           |    .in("books" / "list" / "all")
           |    .out(jsonBody[List[Book]])""".stripMargin
      }

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
        case JsonImplementation.Jsoniter =>
          val codecs = "implicit val codecBooks: JsonValueCodec[List[Book]] = JsonCodecMaker.make"

          Code(
            codecs + System.lineSeparator() + prepareBookListing,
            Set(
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.jsoniter._"),
              Import("com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec"),
              Import("com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker")
            )
          )
        case JsonImplementation.ZIOJson =>
          val codecs =
            """  implicit val authorZioEncoder: zio.json.JsonEncoder[Author] = DeriveJsonEncoder.gen[Author]
              |  implicit val authorZioDecoder: zio.json.JsonDecoder[Author] = DeriveJsonDecoder.gen[Author]
              |  implicit val bookZioEncoder: zio.json.JsonEncoder[Book] = DeriveJsonEncoder.gen[Book]
              |  implicit val bookZioDecoder: zio.json.JsonDecoder[Book] = DeriveJsonDecoder.gen[Book]""".stripMargin

          Code(
            codecs + System.lineSeparator() + prepareBookListing,
            Set(
              Import("sttp.tapir.Codec.JsonCodec"),
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.zio._"),
              Import("zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}")
            )
          )
      }
    }

    private def prepareBookListingServerLogic(starterDetails: StarterDetails): Code = {
      val (serverKind, pureEffectFn) = starterDetails.serverEffect match {
        case ServerEffect.FutureEffect => ("ServerEndpoint[Any, Future]", "Future.successful")
        case ServerEffect.IOEffect     => ("ServerEndpoint[Any, IO]", "IO.pure")
        case ServerEffect.ZIOEffect    => ("ZServerEndpoint[Any, Any]", "ZIO.succeed")
      }

      // Imports silently taken from helloServerEndpoint
      Code(s"val $booksListingServerEndpoint: $serverKind = $bookListing.serverLogicSuccess(_ => $pureEffectFn(books.get()))")
    }

  }

  private object MetricsEndpoint {
    def prepareMetricsEndpoint(serverEffect: ServerEffect): Code = {
      val (effect, endpoint) = serverEffectToEffectAndEndpoint(serverEffect)
      Code(
        s"""  val prometheusMetrics: PrometheusMetrics[${effect}] = PrometheusMetrics.default[${effect}]()
           |  val metricsEndpoint: ${endpoint} = prometheusMetrics.metricsEndpoint""".stripMargin,
        serverEffectImports(serverEffect) + Import("sttp.tapir.server.metrics.prometheus.PrometheusMetrics")
      )
    }
  }

  private object DocumentationEndpoint {

    def prepareDocEndpoints(projectName: String, serverEffect: ServerEffect, addMetrics: Boolean, jsonImplementation: JsonImplementation): Code = {

      val metricsEndpoint = if (addMetrics) List(docMetricsEndpoint) else Nil
      val jsonEndpoint = if (jsonImplementation == JsonImplementation.WithoutJson) Nil else List(bookListing)
      val endpoints: List[String] = List(helloEndpoint) ++ metricsEndpoint ++ jsonEndpoint

      Code(prepareCode(projectName, serverEffect, endpoints), prepareImports(serverEffect))
    }

    private def prepareCode(projectName: String, serverEffect: ServerEffect, endpoints: List[String]): String = {
      val (effect, endpoint) = serverEffectToEffectAndEndpoint(serverEffect)
      s"""  val $docEndpoints: List[${endpoint}] = SwaggerInterpreter().fromEndpoints[${effect}](List(${endpoints.mkString(
          ","
        )}), "$projectName", "1.0.0")""".stripMargin
    }

    def prepareImports(serverEffect: ServerEffect): Set[Import] = {
      serverEffectImports(serverEffect) + Import("sttp.tapir.swagger.bundle.SwaggerInterpreter")
    }
  }

  private def serverEffectToEffectAndEndpoint(serverEffect: ServerEffect): (String, String) = {
    serverEffect match {
      case ServerEffect.FutureEffect => ("Future", "ServerEndpoint[Any, Future]")
      case ServerEffect.IOEffect => ("IO", "ServerEndpoint[Any, IO]")
      case ServerEffect.ZIOEffect => ("Task", "ZServerEndpoint[Any, Any]")
    }
  }

  private def serverEffectImports(serverEffect: ServerEffect): Set[Import] = {
    serverEffect match {
      case ServerEffect.ZIOEffect => Set(Import("zio.Task"))
      case _ => Set.empty[Import]
    }
  }

  object Constants {
    val helloEndpoint = "helloEndpoint"
    val helloServerEndpoint = "helloServerEndpoint"
    val bookListing = "booksListing"
    val booksListingServerEndpoint = "booksListingServerEndpoint"
    val metricsEndpoint = "metricsEndpoint"
    val docMetricsEndpoint = "metricsEndpoint.endpoint"
    val docEndpoints = "docEndpoints"
    val all = "all"
  }
}
