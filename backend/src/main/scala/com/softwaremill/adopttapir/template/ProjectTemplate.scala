package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.sbt.Dependency.{PluginDependency, ScalaDependency}
import os.RelPath

case class FileTemplate(
    relativePath: RelPath,
    content: String
)

class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): FileTemplate = {
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

    FileTemplate(os.RelPath("build.sbt"), content)
  }

  def getBuildProperties(): FileTemplate = {
    FileTemplate(
      os.RelPath("project/build.properties"),
      txt.buildProperties(config.sbtVersion).toString()
    )
  }
  def getSbtPlugins(starterDetails: StarterDetails): FileTemplate = {
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

    FileTemplate(
      os.RelPath("project/plugins.sbt"),
      content
    )
  }
  def getMain(starterDetails: StarterDetails): FileTemplate = {
    val groupId = starterDetails.groupId

    FileTemplate(
      os.RelPath("src/main/scala") / os.RelPath(groupId.split('.').mkString("/")) / "Main.scala",
      txt.Main(groupId).toString()
    )
  }
  def getMainSpec(starterDetails: StarterDetails): FileTemplate = {
    val groupId = starterDetails.groupId

    FileTemplate(
      os.RelPath("src/test/scala") / os.RelPath(groupId.split('.').mkString("/")) / "MainSpec.scala",
      txt.MainSpec(groupId).toString()
    )
  }
}
