package com.softwaremill.adopttapir.http

import cats.effect.std.Dispatcher
import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.infrastructure.CorrelationIdInterceptor
import com.softwaremill.adopttapir.util.ServerEndpoints
import com.typesafe.scalalogging.StrictLogging
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.Method
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.cors.CORSConfig.AllowedMethods
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.vertx.cats.{VertxCatsServerInterpreter, VertxCatsServerOptions}
import sttp.tapir.server.vertx.cats.VertxCatsServerInterpreter._
import sttp.tapir.static.ResourcesOptions
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

/** Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs support.
  *
  * The following endpoints are exposed:
  *   - `/api/v1` - the main API
  *   - `/api/v1/docs` - swagger UI for the main API
  *   - `/admin` - admin API
  *   - `/` - serving frontend resources
  */
class HttpApi(
    http: Http,
    mainEndpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    prometheusMetrics: PrometheusMetrics[IO],
    config: HttpConfig
) extends StrictLogging {
  private val apiContextPath = List("api", "v1")

  private def serverOptions(dispatcher: Dispatcher[IO]): VertxCatsServerOptions[IO] =
    VertxCatsServerOptions
      .customiseInterceptors[IO](dispatcher)
      .prependInterceptor(CorrelationIdInterceptor)
      // all errors are formatted as json, and there are no other additional http4s routes
      .defaultHandlers(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
      // TODO customise the serverLog when available
      .corsInterceptor(CORSInterceptor.default[IO])
      .metricsInterceptor(prometheusMetrics.metricsInterceptor())
      .options

  lazy val allPublicEndpoints: List[ServerEndpoint[Any with Fs2Streams[IO], IO]] = {
    // creating the documentation using `mainEndpoints` without the /api/v1 context path; instead, a server will be added
    // with the appropriate suffix
    val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
      .fromServerEndpoints(mainEndpoints.toList, "adopt-tapir", "1.0")

    // for /api/v1 requests, first trying the API; then the docs
    val apiEndpoints =
      (mainEndpoints ++ docsEndpoints).map(se => se.prependSecurityIn(apiContextPath.foldLeft(emptyInput: EndpointInput[Unit])(_ / _)))

    // for all other requests, first trying getting existing webapp resource (html, js, css files), from the /webapp
    // directory on the classpath; otherwise, returning index.html; this is needed to support paths in the frontend
    // apps (e.g. /login) the frontend app will handle displaying appropriate error messages
    val webappEndpoints = List(
      resourcesGetServerEndpoint[IO](emptyInput: EndpointInput[Unit])(
        classOf[HttpApi].getClassLoader,
        "webapp",
        ResourcesOptions.default.defaultResource(List("index.html"))
      )
    )
    apiEndpoints.toList ++ webappEndpoints
  }

  private lazy val allAdminEndpoints: List[ServerEndpoint[Any with Fs2Streams[IO], IO]] =
    (adminEndpoints ++ List(prometheusMetrics.metricsEndpoint)).toList

  /** The resource describing the HTTP server; binds when the resource is allocated. */
  def resource(dispatcher: Dispatcher[IO]): Resource[IO, (HttpServer, HttpServer)] = {
    def resource(dispatcher: Dispatcher[IO], endpoints: List[ServerEndpoint[Any with Fs2Streams[IO], IO]], port: Int) = {
      Resource.make(
        IO.delay {
          val vertx = Vertx.vertx()
          val server = vertx.createHttpServer()
          val router = Router.router(vertx)
          endpoints
            .map(endpoint => VertxCatsServerInterpreter[IO](serverOptions(dispatcher)).route(endpoint))
            .foreach(attach => attach(router))
          server.requestHandler(router).listen(port, config.host)
        }.flatMap(_.asF[IO])
      )({ server =>
        IO.delay(server.close).flatMap(_.asF[IO].void)
      })
    }

    for {
      public <- resource(dispatcher, allPublicEndpoints, config.port)
      admin <- resource(dispatcher, allAdminEndpoints, config.adminPort)
    } yield (public, admin)
  }
}
