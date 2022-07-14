import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import sbt.Keys._
import sbt._
import complete.DefaultParsers._
import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import scala.sys.process.Process
import scala.util.Try

val tapirVersion = "1.0.1"
val http4sVersion = "0.23.12"
val circeVersion = "0.14.1"
val tsecVersion = "0.4.0"
val sttpVersion = "3.6.2"
val prometheusVersion = "0.16.0"
val macwireVersion = "2.5.7"

val scalafmtVersion = "3.5.8"
val scalaLoggingVersion = "3.9.4"
val logbackClassicVersion = "1.2.11"
val scalaTestVersion = "3.2.12"

val httpDependencies = Seq(
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion
)

val monitoringDependencies = Seq(
  "io.prometheus" % "simpleclient" % prometheusVersion,
  "io.prometheus" % "simpleclient_hotspot" % prometheusVersion,
  "com.softwaremill.sttp.client3" %% "prometheus-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion
)

val jsonDependencies = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % tapirVersion,
  "com.beachape" %% "enumeratum-circe" % "1.7.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackClassicVersion
)

val fileDependencies = Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "org.apache.commons" % "commons-compress" % "1.21"
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.17.1"
)

val baseDependencies = Seq(
  "org.typelevel" %% "cats-effect" % "3.3.14",
  "com.softwaremill.common" %% "tagging" % "2.3.3",
  "com.softwaremill.quicklens" %% "quicklens" % "1.8.8"
)

val apiDocsDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
)

val securityDependencies = Seq(
  "io.github.jmcardon" %% "tsec-password" % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-jca" % tsecVersion
)

val macwireDependencies = Seq(
  "com.softwaremill.macwire" %% "macrosautocats" % macwireVersion
).map(_ % Provided)

val scalafmtStandaloneDependencies = Seq(
  "org.scalameta" %% "scalafmt-dynamic" % scalafmtVersion
)

val unitTestingStack = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % "1.16.0" % Test,
  "com.lihaoyi" %% "os-lib" % "0.8.1" % Test
)

val commonDependencies =
  baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies ++ fileDependencies ++ scalafmtStandaloneDependencies

lazy val uiProjectName = "ui"
lazy val uiDirectory = settingKey[File]("Path to the ui project directory")
lazy val updateYarn = taskKey[Unit]("Update yarn")
lazy val yarnTask = inputKey[Unit]("Run yarn with arguments")
lazy val copyWebapp = taskKey[Unit]("Copy webapp")

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.adopttapir",
  scalaVersion := "2.13.8",
  libraryDependencies ++= commonDependencies,
  uiDirectory := (ThisBuild / baseDirectory).value / uiProjectName,
  updateYarn := {
    streams.value.log("Updating npm/yarn dependencies")
    haltOnCmdResultError(Process("yarn install", uiDirectory.value).!)
  },
  yarnTask := {
    val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
    updateYarn.value
    val localYarnCommand = "yarn " + taskName

    def runYarnTask() = Process(localYarnCommand, uiDirectory.value).!

    streams.value.log("Running yarn task: " + taskName)
    haltOnCmdResultError(runYarnTask())
  }
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    action("lastCommitHash") {
      import scala.sys.process._
      // if the build is done outside of a git repository, we still want it to succeed
      Try("git rev-parse HEAD".!!.trim).getOrElse("?")
    }
  ),
  buildInfoOptions += BuildInfoOption.ToJson,
  buildInfoOptions += BuildInfoOption.ToMap,
  buildInfoPackage := "com.softwaremill.adopttapir.version",
  buildInfoObject := "BuildInfo"
)

lazy val fatJarSettings = Seq(
  assembly / assemblyJarName := "adopttapir.jar",
  assembly := assembly.dependsOn(copyWebapp).value,
  assembly / assemblyMergeStrategy := {
    case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties"       => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "pom.properties"                     => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "scala-collection-compat.properties" => MergeStrategy.first
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

def versionWithTimestamp(version: String): String = s"$version-${System.currentTimeMillis()}"

lazy val dockerSettings = Seq(
  dockerExposedPorts := Seq(8080),
  dockerBaseImage := "adoptopenjdk:11.0.5_10-jdk-hotspot",
  Docker / packageName := "adopttapir",
  dockerUsername := Some("softwaremill"),
  dockerUpdateLatest := true,
  Docker / publishLocal := (Docker / publishLocal).dependsOn(copyWebapp).value,
  Docker / version := git.gitDescribedVersion.value
    .map(versionWithTimestamp)
    .getOrElse(git.formattedShaVersion.value.map(versionWithTimestamp).getOrElse("latest")),
  git.uncommittedSignifier := Some("dirty"),
  ThisBuild / git.formattedShaVersion := {
    val base = git.baseVersion.?.value
    val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)
    git.gitHeadCommit.value.map { sha =>
      git.defaultFormatShaVersion(base, sha.take(7), suffix)
    }
  }
)

def haltOnCmdResultError(result: Int): Unit = {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

def now(): String = {
  import java.text.SimpleDateFormat
  import java.util.Date
  new SimpleDateFormat("yyyy-MM-dd-hhmmss").format(new Date())
}

lazy val rootProject = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "adopt-tapir"
  )
  .aggregate(backend, templateDependencies)

lazy val backend: Project = (project in file("backend"))
  .settings(
    libraryDependencies ++= httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ securityDependencies ++ macwireDependencies,
    Compile / mainClass := Some("com.softwaremill.adopttapir.Main"),
    copyWebapp := {
      val source = uiDirectory.value / "build"
      val target = (Compile / classDirectory).value / "webapp"
      streams.value.log.info(s"Copying the webapp resources from $source to $target")
      IO.copyDirectory(source, target)
    },
    copyWebapp := copyWebapp.dependsOn(yarnTask.toTask(" build")).value,
    Test / testOptions += Tests.Argument("-P" + java.lang.Runtime.getRuntime.availableProcessors())
  )
  .dependsOn(templateDependencies)
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(Revolver.settings)
  .settings(buildInfoSettings)
  .settings(fatJarSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(SbtTwirl)
  .settings(dockerSettings)

val plokhotnyukJsoniterVersion = "2.13.36"
val zioTestVersion = "2.0.0"

lazy val templateDependencies: Project = project
  .settings(
    name := "templateDependencies",
    libraryDependencies ++= List(
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion % Provided,
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Provided,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion % Provided,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % plokhotnyukJsoniterVersion % Provided,
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % plokhotnyukJsoniterVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion % Provided,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion % Provided,
      "com.softwaremill.sttp.client3" %% "jsoniter" % sttpVersion % Provided,
      "com.softwaremill.sttp.client3" %% "zio-json" % sttpVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-cats" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-cats" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion % Provided,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion % Provided
    ),
    buildInfoKeys := Seq[BuildInfoKey](
      "sttpVersion" -> sttpVersion,
      "plokhotnyukJsoniterVersion" -> plokhotnyukJsoniterVersion,
      "tapirVersion" -> tapirVersion,
      "scalaLoggingVersion" -> scalaLoggingVersion,
      "logbackClassicVersion" -> logbackClassicVersion,
      "scalaTestVersion" -> scalaTestVersion,
      "http4sVersion" -> http4sVersion,
      "zioTestVersion" -> zioTestVersion,
      "scalafmtVersion" -> scalafmtVersion,
      "sbtVersion" -> sbtVersion.value
    ),
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoPackage := "com.softwaremill.adopttapir.version",
    buildInfoObject := "TemplateDependencyInfo"
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
