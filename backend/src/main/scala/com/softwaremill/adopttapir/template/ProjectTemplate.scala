package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.sbt.BuildSbtView
import com.softwaremill.adopttapir.template.sbt.Dependency.ScalaDependency
import com.softwaremill.adopttapir.template.scala.{APIDefinitionsSpecView, APIDefinitionsView, MainView}

case class GeneratedFile(
    relativePath: String,
    content: String
)

/** Every method represent one of generated file. As template mechanism Twirl library were used. Which have some crucial limitations for
  * more advanced logic. That's why it is passed to Twirl templates through simple String by using `*View` objects.
  *
  * As an example see @see [[APIDefinitionsView]]
  */
class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {

    val tapirVersion = starterDetails.tapirVersion

    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        config.scalaVersion,
        httpDependencies = BuildSbtView.getHttpDependencies(starterDetails),
        monitoringDependencies = Nil,
        jsonDependencies = Nil,
        baseDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-sttp-client", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-core", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-sttp-stub-server", tapirVersion)
        ),
        docDependencies =
          if (starterDetails.documentationAdded)
            List(ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-bundle", tapirVersion))
          else Nil
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

  def getApiDefinitions(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerEndpoint = APIDefinitionsView.getHelloServerEndpoint(starterDetails)
    val docEndpoints = APIDefinitionsView.getDocEndpoints(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "ApiDefinitions.scala"),
      txt
        .ApiDefinitions(
          starterDetails,
          helloServerEndpoint.additionalImports ++ docEndpoints.additionalImports,
          helloServerEndpoint.logic,
          docEndpoints.logic
        )
        .toString()
    )
  }

  def getApiSpecDefinitions(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerStub = APIDefinitionsSpecView.getHelloServerStub(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "ApiDefinitionsSpec.scala"),
      txt.ApiDefinitionsSpec(starterDetails, helloServerStub.additionalImports, helloServerStub.logic).toString()
    )
  }

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName
}
