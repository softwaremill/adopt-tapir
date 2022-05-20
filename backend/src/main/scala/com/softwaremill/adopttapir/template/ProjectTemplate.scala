package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.sbt.Dependency.{PluginDependency, ScalaDependency}

case class GeneratedFile(
    relativePath: String,
    content: String
)

class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {
    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        config.scalaVersion,
        httpDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-core", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-akka-http-server", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-http4s-server", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-sttp-client", "0.17.19"),
          ScalaDependency("org.http4s", "http4s-circe", "0.21.20")
        ),
        monitoringDependencies = Nil,
        jsonDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-json-circe", "0.17.19")
        ),
        baseDependencies = Nil,
        docDependencies = List(
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-openapi-docs", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-openapi-circe-yaml", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-http4s", "0.17.19"),
          ScalaDependency("com.softwaremill.sttp.tapir", "tapir-swagger-ui-akka-http", "0.17.19")
        )
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
      txt.Main(groupId).toString()
    )
  }
  def getMainSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "MainSpec.scala"),
      txt.MainSpec(groupId).toString()
    )
  }

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName
}
