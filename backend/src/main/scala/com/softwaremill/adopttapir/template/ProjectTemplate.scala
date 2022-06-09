package com.softwaremill.adopttapir.template

import better.files.Resource
import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectTemplate.ScalafmtConfigFile
import com.softwaremill.adopttapir.template.sbt.BuildSbtView
import com.softwaremill.adopttapir.template.scala.{EndpointsSpecView, EndpointsView, MainView}

case class GeneratedFile(
    relativePath: String,
    content: String
)

/** Every method represent one of generated file. As template mechanism Twirl library were used. Which have some crucial limitations for
  * more advanced logic. That's why it is passed to Twirl templates through simple String by using `*View` objects.
  *
  * As an example see @see [[EndpointsView]]
  */
class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {

    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        config.scalaVersion,
        starterDetails.tapirVersion,
        (BuildSbtView.getDependencies _).andThen(BuildSbtView.format)(starterDetails)
      )
      .toString()

    GeneratedFile("build.sbt", content)
  }

  def getBuildProperties: GeneratedFile = GeneratedFile(
    "project/build.properties",
    txt.buildProperties(config.sbtVersion).toString()
  )

  def getMain(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Main.scala"),
      MainView.getProperMainContent(starterDetails)
    )
  }

  def getEndpoints(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerEndpoint = EndpointsView.getHelloServerEndpoint(starterDetails)
    val docEndpoints = EndpointsView.getDocEndpoints(starterDetails)
    val jsonEndpoint = EndpointsView.getJsonOutModel(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Endpoints.scala"),
      txt
        .Endpoints(
          starterDetails,
          helloServerEndpoint.imports ++ docEndpoints.imports ++ jsonEndpoint.imports,
          helloServerEndpoint.body,
          docEndpoints.body,
          jsonEndpoint = jsonEndpoint.body
        )
        .toString()
    )
  }

  def getEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerStub = EndpointsSpecView.getHelloServerStub(starterDetails)
    val unwrapper = EndpointsSpecView.Rich.prepareUnwrapper(starterDetails.serverEffect)

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "EndpointsSpec.scala"),
      txt.EndpointsSpec(starterDetails, helloServerStub.imports ++ unwrapper.imports, helloServerStub.body, unwrapper.body).toString()
    )
  }

  val pluginsSbt: GeneratedFile = GeneratedFile("project/plugins.sbt", templateResource("plugins.sbt"))

  val scalafmtConf: GeneratedFile = GeneratedFile(ScalafmtConfigFile, templateResource("scalafmt.conf"))

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName

  private def templateResource(fileName: String): String = Resource.getAsString(s"template/$fileName")
}

object ProjectTemplate {
  val ScalafmtConfigFile = ".scalafmt.conf"
}
