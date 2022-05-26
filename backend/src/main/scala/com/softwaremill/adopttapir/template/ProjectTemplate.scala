package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.sbt.Dependency.{PluginDependency, ScalaDependency}
import com.softwaremill.adopttapir.template.scala.{APIDefinitionsSpecView, APIDefinitionsView, MainView}

case class GeneratedFile(
    relativePath: String,
    content: String
)

class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {

    val tapirVersion = starterDetails.tapirVersion

    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        config.scalaVersion,
        httpDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-akka-http-server", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-netty-server", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", tapirVersion),
          ScalaDependency("org.http4s", "http4s-blaze-server", "0.23.11"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http4s-server", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-zio-http-server", tapirVersion)
        ),
        monitoringDependencies = Nil,
        jsonDependencies = Nil,
        baseDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-sttp-client", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-core", tapirVersion),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-sttp-stub-server", tapirVersion)
        ),
        docDependencies = Nil
      )
      .toString()

    GeneratedFile("build.sbt", content)
  }

  def getBuildProperties(): GeneratedFile = GeneratedFile(
    "project/build.properties",
    txt.buildProperties(config.sbtVersion).toString()
  )

  def getSbtPlugins(): GeneratedFile = {
    val content =
      txt
        .pluginsSbt(
          List(
            PluginDependency("com.eed3si9n", "sbt-buildinfo", "0.11.0"),
            PluginDependency("io.spray", "sbt-revolver", "0.9.1"),
            PluginDependency("com.heroku", "sbt-heroku", "2.1.4"),
            PluginDependency("com.softwaremill.sbt-softwaremill", "sbt-softwaremill-common", "2.0.9"),
            PluginDependency("com.eed3si9n", "sbt-assembly", "1.2.0"),
            PluginDependency("com.github.sbt", "sbt-native-packager", "1.9.9"),
            PluginDependency("com.github.sbt", "sbt-git", "2.0.0"),
            PluginDependency("com.timushev.sbt", "sbt-updates", "0.6.2")
          )
        )
        .toString()

    GeneratedFile(
      "project/plugins.sbt",
      content
    )
  }
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

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "ApiDefinitions.scala"),
      txt.ApiDefinitions(starterDetails, helloServerEndpoint.additionalImports, helloServerEndpoint.logic).toString()
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
