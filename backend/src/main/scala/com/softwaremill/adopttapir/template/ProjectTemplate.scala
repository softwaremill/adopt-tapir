package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
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

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Endpoints.scala"),
      txt
        .Endpoints(
          starterDetails,
          helloServerEndpoint.imports ++ docEndpoints.imports,
          helloServerEndpoint.body,
          docEndpoints.body
        )
        .toString()
    )
  }

  def getEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerStub = EndpointsSpecView.getHelloServerStub(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "EndpointsSpec.scala"),
      txt.EndpointsSpec(starterDetails, helloServerStub.imports, helloServerStub.body).toString()
    )
  }

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName
}
