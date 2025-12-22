package com.softwaremill.adopttapir.template

sealed trait Dependency:
  val groupId: String
  val artifactId: String
  val version: String
  val excludeGroupId: Option[String]
  val excludeArtifactId: Option[String]

object Dependency:
  val constantTapirVersion = "tapirVersion"

  case class ScalaDependency(
      groupId: String,
      artifactId: String,
      version: String,
      excludeGroupId: Option[String] = None,
      excludeArtifactId: Option[String] = None
  ) extends Dependency

  case class ScalaTestDependency(
      groupId: String,
      artifactId: String,
      version: String,
      excludeGroupId: Option[String] = None,
      excludeArtifactId: Option[String] = None
  ) extends Dependency

  case class JavaDependency(
      groupId: String,
      artifactId: String,
      version: String,
      excludeGroupId: Option[String] = None,
      excludeArtifactId: Option[String] = None
  ) extends Dependency

  trait SbtFormatter[A <: Dependency]:
    def format(dep: A): String

  trait ScalaCliFormatter[A <: Dependency]:
    def format(dep: A): String

  private def formatVersion(version: String): String =
    if version == constantTapirVersion then constantTapirVersion else s"\"$version\""

  private def addExclude(base: String, excludeGroupId: Option[String], excludeArtifactId: Option[String]): String =
    (excludeGroupId, excludeArtifactId) match
      case (Some(gid), Some(aid)) => s"$base exclude(\"$gid\", \"$aid\")"
      case _                       => base

  private def formatScalaSbtDependency(dep: Dependency, suffix: String = ""): String =
    val versionString = formatVersion(dep.version)
    val base = s"\"${dep.groupId}\" %% \"${dep.artifactId}\" % $versionString$suffix"
    addExclude(base, dep.excludeGroupId, dep.excludeArtifactId)

  private def formatJavaSbtDependency(dep: Dependency): String =
    val base = s"\"${dep.groupId}\" % \"${dep.artifactId}\" % \"${dep.version}\""
    addExclude(base, dep.excludeGroupId, dep.excludeArtifactId)

  given SbtFormatter[ScalaDependency] with
    def format(dep: ScalaDependency): String = formatScalaSbtDependency(dep)

  given SbtFormatter[ScalaTestDependency] with
    def format(dep: ScalaTestDependency): String = formatScalaSbtDependency(dep, " % Test")

  given SbtFormatter[JavaDependency] with
    def format(dep: JavaDependency): String = formatJavaSbtDependency(dep)

  private def formatScalaCliDependency(dep: Dependency): String =
    s"${dep.groupId}::${dep.artifactId}:${dep.version}"

  private def formatJavaCliDependency(dep: Dependency): String =
    s"${dep.groupId}:${dep.artifactId}:${dep.version}"

  given ScalaCliFormatter[ScalaDependency] with
    def format(dep: ScalaDependency): String = formatScalaCliDependency(dep)

  given ScalaCliFormatter[ScalaTestDependency] with
    def format(dep: ScalaTestDependency): String = formatScalaCliDependency(dep)

  given ScalaCliFormatter[JavaDependency] with
    def format(dep: JavaDependency): String = formatJavaCliDependency(dep)

  def asSbtDependency(dep: Dependency): String = dep match
    case d: ScalaDependency     => summon[SbtFormatter[ScalaDependency]].format(d)
    case d: ScalaTestDependency => summon[SbtFormatter[ScalaTestDependency]].format(d)
    case d: JavaDependency     => summon[SbtFormatter[JavaDependency]].format(d)

  def asScalaCliDependency(dep: Dependency): String = dep match
    case d: ScalaDependency     => summon[ScalaCliFormatter[ScalaDependency]].format(d)
    case d: ScalaTestDependency => summon[ScalaCliFormatter[ScalaTestDependency]].format(d)
    case d: JavaDependency     => summon[ScalaCliFormatter[JavaDependency]].format(d)
