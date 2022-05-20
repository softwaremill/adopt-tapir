import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import scala.util.Try

val http4sVersion = "0.23.11"
val circeVersion = "0.14.1"
val tsecVersion = "0.4.0"
val sttpVersion = "3.6.1"
val prometheusVersion = "0.15.0"
val tapirVersion = "1.0.0-M9"
val macwireVersion = "2.5.7"

val httpDependencies = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
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
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.codehaus.janino" % "janino" % "3.1.7",
  "de.siegmar" % "logback-gelf" % "4.0.2"
)

val fileDependencies = Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.1",
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.17.1"
)

val baseDependencies = Seq(
  "org.typelevel" %% "cats-effect" % "3.3.11",
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

val scalatest = "org.scalatest" %% "scalatest" % "3.2.12" % Test
val macwireDependencies = Seq(
  "com.softwaremill.macwire" %% "macrosautocats" % macwireVersion
).map(_ % Provided)

val unitTestingStack = Seq(scalatest)

val commonDependencies = baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies ++ fileDependencies

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.adopttapir",
  scalaVersion := "2.13.8",
  libraryDependencies ++= commonDependencies
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

lazy val dockerSettings = Seq(
  dockerExposedPorts := Seq(8080),
  dockerBaseImage := "adoptopenjdk:11.0.5_10-jdk-hotspot",
  Docker / packageName := "adopttapir",
  dockerUsername := Some("softwaremill"),
  dockerUpdateLatest := true,
  Docker / publishLocal := (Docker / publishLocal).value,
  Docker / version := git.gitDescribedVersion.value.getOrElse(git.formattedShaVersion.value.getOrElse("latest")),
  git.uncommittedSignifier := Some("dirty"),
  ThisBuild / git.formattedShaVersion := {
    val base = git.baseVersion.?.value
    val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)
    git.gitHeadCommit.value.map { sha =>
      git.defaultFormatShaVersion(base, sha.take(7), suffix)
    }
  }
)

def haltOnCmdResultError(result: Int) {
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
    name := "adopt-tapir",
    Compile / herokuFatJar := Some((backend / assembly / assemblyOutputPath).value),
    Compile / deployHeroku := ((Compile / deployHeroku) dependsOn (backend / assembly)).value
  )
  .aggregate(backend)

lazy val backend: Project = (project in file("backend"))
  .settings(
    libraryDependencies ++= httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ securityDependencies ++ macwireDependencies,
    Compile / mainClass := Some("com.softwaremill.adopttapir.Main")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(Revolver.settings)
  .settings(buildInfoSettings)
  .settings(fatJarSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(SbtTwirl)
  .settings(dockerSettings)
