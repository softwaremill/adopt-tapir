package com.softwaremill.adopttapir.starter

import com.softwaremill.adopttapir.template.{GeneratedFile, SbtProjectTemplate, ScalaCliProjectTemplate}

object FilesGenerator {
  implicit class StarterDetailsWithFilesGenerator(starterDetails: StarterDetails) {
    def generateFiles: List[GeneratedFile] = {
      starterDetails.builder match {
        case Builder.Sbt      => SbtFilesGenerator.generate(starterDetails)
        case Builder.ScalaCli => ScalaCliFilesGenerator.generate(starterDetails)
      }
    }
  }
}

trait FilesGenerator {
  def generate(starterDetails: StarterDetails): List[GeneratedFile]
}

private case object SbtFilesGenerator extends FilesGenerator {
  private lazy val sbtTemplate: SbtProjectTemplate = new SbtProjectTemplate()

  override def generate(starterDetails: StarterDetails): List[GeneratedFile] = {
    List(
      sbtTemplate.getBuildSbt(starterDetails),
      sbtTemplate.getBuildProperties,
      sbtTemplate.getMain(starterDetails),
      sbtTemplate.getEndpoints(starterDetails),
      sbtTemplate.getEndpointsSpec(starterDetails),
      sbtTemplate.pluginsSbt,
      sbtTemplate.scalafmtConf(starterDetails.scalaVersion),
      sbtTemplate.sbtx,
      sbtTemplate.README
    )
  }
}

private case object ScalaCliFilesGenerator extends FilesGenerator {
  private lazy val scalaCliTemplate: ScalaCliProjectTemplate = new ScalaCliProjectTemplate()

  override def generate(starterDetails: StarterDetails): List[GeneratedFile] = {
    List(
      scalaCliTemplate.getBuildScalaCli(starterDetails),
      scalaCliTemplate.getTestScalaCli(starterDetails),
      scalaCliTemplate.getMain(starterDetails),
      scalaCliTemplate.getEndpoints(starterDetails),
      scalaCliTemplate.getEndpointsSpec(starterDetails),
      scalaCliTemplate.scalafmtConf(starterDetails.scalaVersion),
      scalaCliTemplate.README
    )
  }
}
