package com.softwaremill.adopttapir.example

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import sttp.tapir.{PublicEndpoint, endpoint, query, stringBody}
import zio.Task

import java.util.concurrent.atomic.AtomicReference

object ApiDefinitionsZIOJson {
  case class User(name: String) extends AnyVal

  val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint.get
    .in("hello")
    .in(query[User]("name"))
    .out(stringBody)

  // endpoint descriptions
  case class Author(name: String)

  case class Book(title: String, year: Int, author: Author)

  // zio-json imports

  import sttp.tapir.Codec.JsonCodec
  import sttp.tapir.generic.auto._
  import sttp.tapir.json.zio._
  import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

  implicit val authorZioEncoder: zio.json.JsonEncoder[Author] = DeriveJsonEncoder.gen[Author]
  implicit val authorZioDecoder: zio.json.JsonDecoder[Author] = DeriveJsonDecoder.gen[Author]
  implicit val bookZioEncoder: zio.json.JsonEncoder[Book] = DeriveJsonEncoder.gen[Book]
  implicit val bookZioDecoder: zio.json.JsonDecoder[Book] = DeriveJsonDecoder.gen[Book]

  val bookCodec: JsonCodec[Book] = zioCodec[Book]

  val booksListing: PublicEndpoint[Unit, Unit, Vector[Book], Any] = endpoint.get
    .in("books")
    .in("list" / "all")
    .out(jsonBody[Vector[Book]])

  val books = new AtomicReference(
    Vector(
      Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
      Book("Iliad", -8000, Author("Homer")),
      Book("Nad Niemnem", 1888, Author("Eliza Orzeszkowa")),
      Book("The Colour of Magic", 1983, Author("Terry Pratchett")),
      Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
      Book("Pharaoh", 1897, Author("Boleslaw Prus"))
    )
  )

  import cats.implicits.catsSyntaxEitherId

  import scala.concurrent.{ExecutionContext, Future}

  implicit val ec = ExecutionContext.global
  val helloServerEndpointFuture: ServerEndpoint[Any, Future] = helloEndpoint.serverLogic(user =>
    Future.successful {
      s"Hello ${user.name}".asRight[Unit]
    }
  )

  val booksListingServerEndpointFuture: ServerEndpoint[Any, Future] =
    booksListing.serverLogicSuccess(_ => Future.successful(books.get()))

  import cats.effect.IO

  val helloServerEndpointIO: ServerEndpoint[Any, IO] = helloEndpoint.serverLogic(user =>
    IO.pure {
      s"Hello ${user.name}".asRight[Unit]
    }
  )
  val booksListingServerEndpointIO: ServerEndpoint[Vector[Book], IO] = booksListing.serverLogicSuccess(_ => IO.pure(books.get()))

  import zio.ZIO

  val helloServerEndpointZIO: ZServerEndpoint[Any, Any] = helloEndpoint.zServerLogic(user =>
    ZIO.succeed {
      s"Hello ${user.name}"
    }
  )

  val booksListingServerEndpointZIO: ZServerEndpoint[Any, Any] = booksListing.serverLogicSuccess(_ => ZIO.succeed(books.get()))

  val docsFuture: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](List(helloEndpoint, booksListing), "The tapir library", "1.0.0")

  val docsIO: List[ServerEndpoint[Any, IO]] =
    SwaggerInterpreter().fromEndpoints[IO](List(helloEndpoint, booksListing), "The tapir library", "1.0.0")

  //  val docsZIO: List[ServerEndpoint[Any, RIO[Any, *]]] =
  //    SwaggerInterpreter().fromEndpoints[RIO[Any, *]](List(helloEndpoint), "The tapir library", "1.0.0")

  val docsZIO: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter().fromEndpoints[Task](List(helloEndpoint, booksListing), "The tapir library", "1.0.0")
}
