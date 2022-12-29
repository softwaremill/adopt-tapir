package com.softwaremill.adopttapir.http

import cats.effect.{IO, Resource}
import com.softwaremill.adopttapir.infrastructure.CorrelationIdInterceptor
import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.util.ServerEndpoints
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
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
import scala.util.chaining.*

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
) extends FLogging:

  private val apiContextPath = List("api", "v1")

  private val serverOptions: Http4sServerOptions[IO] = Http4sServerOptions
    .customiseInterceptors[IO]
    .prependInterceptor(CorrelationIdInterceptor)
    // all errors are formatted as json, and there are no other additional http4s routes
    .defaultHandlers(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
    .serverLog {
      // using a context-aware logger for http logging
      Http4sServerOptions
        .defaultServerLog[IO]
        .doLogWhenHandled((msg, e) => e.fold(logger.debug[IO](msg))(logger.debug(msg, _)))
        .doLogAllDecodeFailures((msg, e) => e.fold(logger.debug[IO](msg))(logger.debug(msg, _)))
        .doLogExceptions((msg, e) => logger.error[IO](msg, e))
        .doLogWhenReceived(msg => logger.debug[IO](msg))
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
    def resource(routes: HttpRoutes[IO], port: Int, banner: Option[String]) =
      BlazeServerBuilder[IO]
        .bindHttp(port, config.host.toString)
        .withHttpApp(routes.orNotFound)
        .pipe(s => banner.fold(s.withoutBanner)(b => s.withBanner(b.linesIterator.toSeq)))
        .resource

    for
      public <- resource(publicRoutes, config.port.value, Some(Banner.text))
      admin <- resource(adminRoutes, config.adminPort.value, None)
    yield (public, admin)

end HttpApi
