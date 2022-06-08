package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{JsonImplementation, ServerEffect, StarterDetails}
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants._

object EndpointsView {

  def getHelloServerEndpoint(starterDetails: StarterDetails): Code = starterDetails.serverEffect match {
    case ServerEffect.FutureEffect => HelloServerEndpoint.future
    case ServerEffect.IOEffect     => HelloServerEndpoint.io
    case ServerEffect.ZIOEffect    => HelloServerEndpoint.zio
  }

  def getDocEndpoints(starterDetails: StarterDetails): Code = {
    if (starterDetails.addDocumentation) {
      DocumentationEndpoint.prepareDocEndpoints(starterDetails.projectName, starterDetails.serverEffect, starterDetails.jsonImplementation)
    } else
      Code.empty
  }

  def getJsonOutModel(starterDetails: StarterDetails): Code = {
    JsonModelObject.prepareJsonEndpoint(starterDetails)
  }

  private object HelloServerEndpoint {

    val future: Code = Code(
      s"""  val $helloServerEndpoint: ServerEndpoint[Any, Future] = helloEndpoint.serverLogicSuccess(user =>
         |    Future.successful(s"Hello $${user.name}")
         |  )""".stripMargin,
      List(
        Import("scala.concurrent.ExecutionContext.Implicits.global"),
        Import("scala.concurrent.Future"),
        Import("sttp.tapir._"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val io: Code = Code(
      s"""  val $helloServerEndpoint: ServerEndpoint[Any, IO] = helloEndpoint.serverLogicSuccess(user =>
         |    IO.pure(s"Hello $${user.name}")
         |  )
         |""".stripMargin,
      List(
        Import("cats.effect.IO"),
        Import("sttp.tapir._"),
        Import("sttp.tapir.server.ServerEndpoint")
      )
    )

    val zio: Code = Code(
      s"""  val $helloServerEndpoint: ZServerEndpoint[Any, Any] = helloEndpoint.zServerLogic(user =>
         |    ZIO.succeed(s"Hello $${user.name}")
         |  )
         |""".stripMargin,
      List(
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
          List(prepareModel(), prepareBookListing(starterDetails), prepareBookListingServerLogic(starterDetails))
            .reduce((a, b) => Code(a.body + System.lineSeparator() + b.body, a.imports ++ b.imports))
      }
    }

    private def prepareModel(): Code = Code(
      """  case class Author(name: String)
        |  case class Book(title: String, year: Int, author: Author)

        |  val books = new AtomicReference(
        |    Vector(
        |      Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
        |      Book("Nad Niemnem", 1888, Author("Eliza Orzeszkowa")),
        |      Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
        |      Book("Pharaoh", 1897, Author("Boleslaw Prus"))
        |    )
        |  )""".stripMargin,
      List(Import("java.util.concurrent.atomic.AtomicReference"))
    )

    private def prepareBookListing(starterDetails: StarterDetails): Code = {
      def prepareBookListing: String = {
        s"""  val $bookListing: PublicEndpoint[Unit, Unit, Vector[Book], Any] = endpoint.get
           |    .in("books")
           |    .in("list" / "all")
           |    .out(jsonBody[Vector[Book]])""".stripMargin
      }

      starterDetails.jsonImplementation match {
        case JsonImplementation.WithoutJson => Code.empty
        case JsonImplementation.Circe =>
          Code(
            prepareBookListing,
            List(
              Import("io.circe.generic.auto._"),
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.circe._")
            )
          )
        case JsonImplementation.Jsoniter =>
          Code(
            "implicit val codecBooks: JsonValueCodec[Vector[Book]] = JsonCodecMaker.make" +
              System.lineSeparator() + prepareBookListing,
            List(
              Import("sttp.tapir.generic.auto._"),
              Import("sttp.tapir.json.jsoniter._"),
              Import("com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec"),
              Import("com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker")
            )
          )
        case JsonImplementation.ZIOJson =>
          Code(
            """  implicit val authorZioEncoder: zio.json.JsonEncoder[Author] = DeriveJsonEncoder.gen[Author]
              |  implicit val authorZioDecoder: zio.json.JsonDecoder[Author] = DeriveJsonDecoder.gen[Author]
              |  implicit val bookZioEncoder: zio.json.JsonEncoder[Book] = DeriveJsonEncoder.gen[Book]
              |  implicit val bookZioDecoder: zio.json.JsonDecoder[Book] = DeriveJsonDecoder.gen[Book]""".stripMargin +
              System.lineSeparator() + prepareBookListing,
            List(
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

  private object DocumentationEndpoint {

    def prepareDocEndpoints(projectName: String, serverEffect: ServerEffect, jsonImplementation: JsonImplementation): Code = {

      val jsonEndpoint = if (jsonImplementation == JsonImplementation.WithoutJson) Nil else List(bookListing)
      val endpoints: List[String] = List(helloEndpoint) ++ jsonEndpoint

      Code(prepareCode(projectName, serverEffect, endpoints), prepareImports(serverEffect))
    }

    private def prepareCode(projectName: String, serverEffect: ServerEffect, endpoints: List[String]): String = {
      val effectStr = serverEffect match {
        case ServerEffect.FutureEffect => ("ServerEndpoint[Any, Future]", "Future")
        case ServerEffect.IOEffect     => ("ServerEndpoint[Any, IO]", "IO")
        case ServerEffect.ZIOEffect    => ("ZServerEndpoint[Any, Any]", "Task")
      }
      s"""  val $docEndpoints: List[${effectStr._1}] = SwaggerInterpreter().fromEndpoints[${effectStr._2}](List(${endpoints.mkString(
          ","
        )}), "${projectName}", "1.0.0")""".stripMargin
    }

    def prepareImports(serverEffect: ServerEffect): List[Import] = {
      Import("sttp.tapir.swagger.bundle.SwaggerInterpreter") ::
        (serverEffect match {
          case ServerEffect.ZIOEffect => List(Import("zio.Task"))
          case _                      => Nil
        })
    }
  }

  object Constants {
    val helloEndpoint = "helloEndpoint"
    val helloServerEndpoint = "helloServerEndpoint"
    val bookListing = "booksListing"
    val booksListingServerEndpoint = "booksListingServerEndpoint"
    val docEndpoints = "docEndpoints"
  }
}
