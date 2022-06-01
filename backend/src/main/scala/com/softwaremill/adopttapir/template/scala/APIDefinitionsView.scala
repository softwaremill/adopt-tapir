package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.{ServerEffect, StarterDetails}

object APIDefinitionsView {

  def getHelloServerEndpoint(starterDetails: StarterDetails): PlainLogicWithImports = starterDetails.serverEffect match {
    case ServerEffect.FutureEffect => HelloServerEndpoint.future
    case ServerEffect.IOEffect     => HelloServerEndpoint.io
    case ServerEffect.ZIOEffect    => HelloServerEndpoint.zio
  }

  def getDocEndpoints(starterDetails: StarterDetails): PlainLogicWithImports = {
    if (starterDetails.addDocumentation) {
      DocumentationEndpoint.prepareDocEndpoints(starterDetails.projectName, starterDetails.serverEffect)
    } else
      PlainLogicWithImports.empty
  }

  private object HelloServerEndpoint {

    val future: PlainLogicWithImports = PlainLogicWithImports(
      """  val helloServerEndpoint: Full[Unit, Unit, User, Unit, String, Any, Future] = helloEndpoint.serverLogic(user =>
        |    Future.successful {
        |      s"Hello ${user.name}".asRight[Unit]
        |    }
        |  )""".stripMargin,
      List(
        Import("scala.concurrent.ExecutionContext.Implicits.global"),
        Import("scala.concurrent.Future"),
        Import("cats.implicits.catsSyntaxEitherId")
      )
    )

    val io: PlainLogicWithImports = PlainLogicWithImports(
      """  val helloServerEndpoint: Full[Unit, Unit, User, Unit, String, Any, IO] = helloEndpoint.serverLogic(user =>
        |    IO.pure {
        |      s"Hello ${user.name}".asRight[Unit]
        |    }
        |  )
        |""".stripMargin,
      List(
        Import("cats.effect.IO"),
        Import("cats.implicits.catsSyntaxEitherId")
      )
    )

    val zio: PlainLogicWithImports = PlainLogicWithImports(
      """  val helloServerEndpoint: ZServerEndpoint[Any, Any] = helloEndpoint.zServerLogic(user =>
        |    ZIO.succeed {
        |      s"Hello ${user.name}"
        |    }
        |  )
        |""".stripMargin,
      List(
        Import("sttp.tapir.ztapir._"),
        Import("zio.ZIO")
      )
    )
  }

  private object DocumentationEndpoint {

    def prepareDocEndpoints(projectName: String, serverEffect: ServerEffect): PlainLogicWithImports = {
      val endpoints: List[String] = List("helloEndpoint")

      PlainLogicWithImports(prepareCode(projectName, serverEffect, endpoints), prepareImports(serverEffect))
    }

    private def prepareCode(projectName: String, serverEffect: ServerEffect, endpoints: List[String]): String = {
      val effectStr = serverEffect match {
        case ServerEffect.FutureEffect => ("ServerEndpoint[Any, Future]", "Future")
        case ServerEffect.IOEffect     => ("ServerEndpoint[Any, IO]", "IO")
        case ServerEffect.ZIOEffect    => ("ZServerEndpoint[Any, Any]", "Task")
      }
      s"""  val docEndpoints: List[${effectStr._1}] = SwaggerInterpreter().fromEndpoints[${effectStr._2}](List(${endpoints.mkString(
          ","
        )}), "${projectName}", "1.0.0")""".stripMargin
    }

    def prepareImports(serverEffect: ServerEffect): List[Import] = {
      Import("sttp.tapir.swagger.bundle.SwaggerInterpreter") ::
        (serverEffect match {
          case ServerEffect.ZIOEffect => List(Import("zio.Task"))
          case _                      => List(Import("sttp.tapir.server.ServerEndpoint"))
        })
    }
  }
}
