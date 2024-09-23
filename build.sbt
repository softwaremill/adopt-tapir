import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import complete.DefaultParsers._
import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import scala.sys.process.Process
import scala.util.Try

val scala2Version = "2.13.14"
val scala3Version = "3.5.0"

val tapirVersion = "1.11.5"

val http4sEmberServerVersion = "0.23.28"
val http4sCirceVersion = "0.23.28"
val circeVersion = "0.14.10"
val circeGenericsExtrasVersion = "0.14.3"
val sttpVersion = "3.9.8"
val prometheusVersion = "0.16.0"
val scalafmtVersion = "3.8.3"
val scalaLoggingVersion = "3.9.5"
val logbackClassicVersion = "1.5.8"
val scalaTestVersion = "3.2.19"
val plokhotnyukJsoniterVersion = "2.30.11"
val zioTestVersion = "2.0.13"

val httpDependencies = Seq(
  "org.http4s" %% "http4s-ember-server" % http4sEmberServerVersion,
  "org.http4s" %% "http4s-circe" % http4sCirceVersion,
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
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  "org.latestbit" %% "circe-tagged-adt-codec" % "0.11.0"
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
  "org.codehaus.janino" % "janino" % "3.1.12" % Runtime,
  "net.logstash.logback" % "logstash-logback-encoder" % "8.0" % Runtime
)

val fileDependencies = Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2" cross CrossVersion.for3Use2_13,
  "org.apache.commons" % "commons-compress" % "1.27.1"
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.7"
)

val baseDependencies = Seq(
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "com.softwaremill.common" %% "tagging" % "2.3.5",
  "com.softwaremill.quicklens" %% "quicklens" % "1.9.9"
)

val apiDocsDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
)

val scalafmtStandaloneDependencies = Seq(
  "org.scalameta" %% "scalafmt-dynamic" % scalafmtVersion cross CrossVersion.for3Use2_13
)

val unitTestingStack = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
  "com.lihaoyi" %% "os-lib" % "0.10.7" % Test
)

val commonDependencies =
  baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies ++ fileDependencies ++ scalafmtStandaloneDependencies

lazy val uiProjectName = "ui"
lazy val uiDirectory = settingKey[File]("Path to the ui project directory")
lazy val updateYarn = taskKey[Unit]("Update yarn")
lazy val yarnTask = inputKey[Unit]("Run yarn with arguments")
lazy val copyWebapp = taskKey[Unit]("Copy webapp")

lazy val commonSettings =
  commonSmlBuildSettings ++
    Seq(
      organization := "com.softwaremill.adopttapir",
      scalaVersion := scala3Version,
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
  dockerBaseImage := "eclipse-temurin:17.0.4_8-jdk-jammy",
  Docker / packageName := "adopttapir",
  dockerUsername := Some("softwaremill"),
  dockerUpdateLatest := true,
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
  .aggregate(backend, ui, templateDependencies)

lazy val ItTest = config("ItTest") extend Test

def itFilter(name: String): Boolean = name endsWith "ITTest"
def unitFilter(name: String): Boolean = (name endsWith "Test") && !itFilter(name)

lazy val backend: Project = (project in file("backend"))
  .configs(ItTest)
  .settings(commonSettings)
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++=
      httpDependencies
        ++ jsonDependencies
        ++ apiDocsDependencies
        ++ monitoringDependencies
  )
  .settings(
    inConfig(ItTest)(Defaults.testTasks),
    Compile / mainClass := Some("com.softwaremill.adopttapir.Main"),
    copyWebapp := {
      val source = uiDirectory.value / "build"
      val target = (Compile / classDirectory).value / "webapp"
      streams.value.log.info(s"Copying the webapp resources from $source to $target")
      IO.copyDirectory(source, target)
    },
    copyWebapp := copyWebapp.dependsOn(yarnTask.toTask(" build")).value,
    Compile / packageBin := ((Compile / packageBin) dependsOn copyWebapp).value,
    Test / testOptions := Seq(Tests.Filter(unitFilter)) ++ Seq(Tests.Argument("-P" + java.lang.Runtime.getRuntime.availableProcessors())),
    ItTest / testOptions := Seq(Tests.Filter(itFilter)) ++ Seq(
      Tests.Argument(
        "-P" + sys.env.getOrElse("IT_TESTS_THREADS_NO", java.lang.Math.min(java.lang.Runtime.getRuntime.availableProcessors() / 2, 4))
      )
    ),
    ItTest / logBuffered := false
  )
  .settings(dockerSettings)
  .settings(Revolver.settings)
  .settings(buildInfoSettings)
  .settings(fatJarSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(SbtTwirl)
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(templateDependencies)

lazy val ui = (project in file(uiProjectName))
  .settings(commonSettings)
  .settings(Test / test := (Test / test).dependsOn(yarnTask.toTask(" lint:check")).dependsOn(yarnTask.toTask(" test")).value)
  .settings(cleanFiles += baseDirectory.value / "build")

lazy val templateDependencies: Project = project
  .settings(
    name := "templateDependencies",
    scalaVersion := scala3Version,
    libraryDependencies ++= List(
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
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-cats" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-cats" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion % Provided,
      "org.http4s" %% "http4s-ember-server" % http4sEmberServerVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-vertx-server" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-vertx-server-cats" % tapirVersion % Provided,
      "com.softwaremill.sttp.tapir" %% "tapir-vertx-server-zio" % tapirVersion % Provided
    ),
    buildInfoKeys := Seq[BuildInfoKey](
      "scala2Version" -> scala2Version,
      "scala3Version" -> scala3Version,
      "sttpVersion" -> sttpVersion,
      "plokhotnyukJsoniterVersion" -> plokhotnyukJsoniterVersion,
      "tapirVersion" -> tapirVersion,
      "logbackClassicVersion" -> logbackClassicVersion,
      "scalaTestVersion" -> scalaTestVersion,
      "http4sEmberServerVersion" -> http4sEmberServerVersion,
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
