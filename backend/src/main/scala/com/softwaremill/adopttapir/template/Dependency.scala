package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.template.Dependency.constantTapirVersion

sealed trait Dependency {
  val groupId: String
  val artifactId: String
  val version: String

  def asSbtDependency: String

  def asScalaCliDependency: String
}

trait DefaultFormat { self: Dependency =>
  override def asSbtDependency: String = s"\"$groupId\" % \"$artifactId\" % \"$version\""

  override def asScalaCliDependency: String = s"\"$groupId:$artifactId:$version\""
}

trait ScalaFormat { self: Dependency =>
  override def asSbtDependency: String = {
    val versionString = if (version == Dependency.constantTapirVersion) constantTapirVersion else s"\"$version\""

    s"\"$groupId\" %% \"$artifactId\" % $versionString"
  }

  override def asScalaCliDependency: String = s"\"$groupId::$artifactId:$version\""
}

trait TestFormat extends ScalaFormat { self: Dependency =>
  override def asSbtDependency: String = super.asSbtDependency + s" % Test"
}

object Dependency {
  val constantTapirVersion = "tapirVersion"

  case class ScalaDependency(groupId: String, artifactId: String, version: String) extends Dependency with ScalaFormat
  case class ScalaTestDependency(groupId: String, artifactId: String, version: String) extends Dependency with TestFormat
  case class JavaDependency(groupId: String, artifactId: String, version: String) extends Dependency with DefaultFormat
}
