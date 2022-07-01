package com.softwaremill.adopttapir.template.sbt

import com.softwaremill.adopttapir.template.sbt.Dependency.constantTapirVersion

sealed trait Dependency {
  val groupId: String
  val artifactId: String
  val version: String

  def asSbtDependency: String
}

trait DefaultFormat { self: Dependency =>
  override def asSbtDependency: String = s"\"$groupId\" % \"$artifactId\" % \"$version\""
}

trait ScalaFormat { self: Dependency =>
  override def asSbtDependency: String = {
    val versionString = if (version == Dependency.constantTapirVersion) constantTapirVersion else s"\"$version\""

    s"\"$groupId\" %% \"$artifactId\" % $versionString"
  }
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
