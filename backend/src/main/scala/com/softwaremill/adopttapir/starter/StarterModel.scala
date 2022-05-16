package com.softwaremill.adopttapir.starter

case class StarterDetails(
    tapirVersion: String,
    scalaVersion: String,
    sbtVersion: String,
    projectDetails: ProjectDetails,
    moduleDependencies: ModuleDependencies
)

case class ProjectDetails(group: String, artifact: String, projectName: String, packageName: String)
case class ModuleDependencies(dependencies: List[String])
case class Dependency(value: String)


object StarterDetails {
  val default: StarterDetails = StarterDetails(
    tapirVersion = "1.0.0",
    scalaVersion = "2.13",
    sbtVersion = "1.6.2",
    projectDetails = ProjectDetails("com.softwaremill", "library", "projectName", "com.softwaremill"),
    ModuleDependencies(Nil)
  )

}
