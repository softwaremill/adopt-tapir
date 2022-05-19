package com.softwaremill.adopttapir.template.sbt

sealed trait Dependency {
  val groupId: String
  val artifactId: String
  val version: String

  def asSbtDependency: String
}

trait DefaultFormat { self: Dependency =>
  override def asSbtDependency: String = s"\"$groupId\" % \"$artifactId\" % \"$version\""
}

trait ScalaVersionFormat { self: Dependency =>
  override def asSbtDependency: String = s"\"$groupId\" %% \"$artifactId\" % \"$version\""
}

object Dependency{
  case class ScalaDependency(groupId: String, artifactId: String, version: String) extends Dependency with ScalaVersionFormat

  case class JavaDependency(groupId: String, artifactId: String, version: String) extends Dependency with DefaultFormat

  case class PluginDependency(groupId: String, artifactId: String, version: String) extends Dependency with DefaultFormat
}


