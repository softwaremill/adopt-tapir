package com.softwaremill.adopttapir.http

import cats.effect.{IO, Resource}
import com.comcast.ip4s.Port
import com.softwaremill.adopttapir.infrastructure.{CorrelationId, CorrelationIdInterceptor}
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.util.ServerEndpoints
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.static.ResourcesOptions
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

/** Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs support.
  *
  * The following endpoints are exposed on `config.port`
  *   - `/api/v1` - the main API
  *   - `/api/v1/docs` - swagger UI for the main API
  *   - `/` - serving frontend resources
  *
  * The following endpoints are exposed on `config.adminPort`
  *   - `/` - admin API
  */
class HttpApi(
    http: Http,
    mainEndpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    prometheusMetrics: PrometheusMetrics[IO],
    config: HttpConfig
)(using c: CorrelationId)
    extends FLogging:

  private val apiContextPath = List("api", "v1")

  private val serverOptions: Http4sServerOptions[IO] = Http4sServerOptions
    .customiseInterceptors[IO]
    .prependInterceptor(CorrelationIdInterceptor.create)
    // all errors are formatted as json, and there are no other additional http4s routes
    .defaultHandlers(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
    .serverLog {
      // using a context-aware logger for http logging
      Http4sServerOptions
        .defaultServerLog[IO]
        .doLogWhenHandled((msg, e) => e.fold(logger.debug(msg))(logger.debug(msg, _)))
        .doLogAllDecodeFailures((msg, e) => e.fold(logger.debug(msg))(logger.debug(msg, _)))
        .doLogExceptions((msg, e) => logger.error(msg, e))
        .doLogWhenReceived(msg => logger.debug(msg))
    }
    .corsInterceptor(CORSInterceptor.default[IO])
    .metricsInterceptor(prometheusMetrics.metricsInterceptor())
    .options

  private lazy val publicRoutes: HttpRoutes[IO] = Http4sServerInterpreter(serverOptions).toRoutes(allPublicEndpoints)
  private lazy val adminRoutes: HttpRoutes[IO] = Http4sServerInterpreter(serverOptions).toRoutes(allAdminEndpoints)

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
  lazy val resource: Resource[IO, (org.http4s.server.Server, org.http4s.server.Server)] =
    def resource(routes: HttpRoutes[IO], port: Port) =
      EmberServerBuilder
        .default[IO]
        .withPort(port)
        .withHost(config.host)
        .withHttpApp(routes.orNotFound)
        .build

    for
      public <- resource(publicRoutes, config.port)
      admin <- resource(adminRoutes, config.adminPort)
    yield (public, admin)

end HttpApi
