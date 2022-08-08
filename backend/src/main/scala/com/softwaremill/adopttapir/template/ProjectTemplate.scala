package com.softwaremill.adopttapir.template

import better.files.Resource
import com.softwaremill.adopttapir.starter.ServerEffect.ZIOEffect
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterDetails}
import com.softwaremill.adopttapir.template.CommonObjectTemplate.legalizeGroupId
import com.softwaremill.adopttapir.template.SbtProjectTemplate._
import com.softwaremill.adopttapir.template.scala.{EndpointsSpecView, EndpointsView, Import, MainView}
import com.softwaremill.adopttapir.version.TemplateDependencyInfo

case class GeneratedFile(
    relativePath: String,
    content: String
)

/** Every method represent one of generated file. As template mechanism Twirl library were used. Which have some crucial limitations for
  * more advanced logic. That's why it is passed to Twirl templates through simple String by using `*View` objects.
  *
  * As an example see @see [[EndpointsView]]
  */
abstract class ProjectTemplate {
  import CommonObjectTemplate.StarterDetailsWithLegalizedGroupId

  def getMain(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Main.scala"),
      MainView.getProperMainContent(starterDetails)
    )
  }

  def getEndpoints(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    val helloServerEndpoint = EndpointsView.getHelloServerEndpoint(starterDetails)
    val docEndpoints = EndpointsView.getDocEndpoints(starterDetails)
    val metricsEndpoint = EndpointsView.getMetricsEndpoint(starterDetails)
    val jsonEndpoint = EndpointsView.getJsonOutModel(starterDetails)
    val library = EndpointsView.getJsonLibrary(starterDetails)
    val allEndpoints = EndpointsView.getAllEndpoints(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Endpoints.scala"),
      txt
        .Endpoints(
          starterDetails,
          toSortedList(
            helloServerEndpoint.imports ++ metricsEndpoint.imports ++ docEndpoints.imports
              ++ jsonEndpoint.imports ++ library.imports ++ allEndpoints.imports
          ),
          helloServerEndpoint.body,
          metricsEndpoint.body,
          docEndpoints.body,
          jsonEndpoint = jsonEndpoint.body,
          library.body,
          allEndpoints.body,
          starterDetails.scalaVersion
        )
        .toString()
    )
  }

  def getEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    val helloServerStub = EndpointsSpecView.getHelloServerStub(starterDetails)
    val booksServerStub = EndpointsSpecView.getBookServerStub(starterDetails)

    val fileContent =
      if (starterDetails.serverEffect == ZIOEffect) {
        txt
          .EndpointsSpecZIO(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports),
            helloServerStub.body,
            booksServerStub.body,
            starterDetails.scalaVersion
          )
      } else {
        val unwrapper = EndpointsSpecView.Unwrapper.prepareUnwrapper(starterDetails.serverEffect, starterDetails.scalaVersion)
        txt
          .EndpointsSpec(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports ++ unwrapper.imports),
            helloServerStub.body,
            unwrapper.body,
            booksServerStub.body,
            starterDetails.scalaVersion
          )
      }

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "EndpointsSpec.scala"),
      fileContent.toString
    )
  }

  import com.softwaremill.adopttapir.template.CommonObjectTemplate.scalafmtConfigPath
  val scalafmtConf: ScalaVersion => GeneratedFile = dialectVersion =>
    GeneratedFile(scalafmtConfigPath, txt.scalafmt(TemplateDependencyInfo.scalafmtVersion, dialectVersion).toString())

  protected def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName

  private def toSortedList(set: Set[Import]): List[Import] = set.toList.sortBy(_.fullName)
}

class SbtProjectTemplate extends ProjectTemplate {
  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {
    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        starterDetails.scalaVersion.value,
        TemplateDependencyInfo.tapirVersion,
        (BuildSbtView.getAllDependencies _).andThen(BuildSbtView.format)(starterDetails),
        starterDetails.serverEffect == ZIOEffect
      )
      .toString()

    GeneratedFile("build.sbt", content)
  }

  def getBuildProperties: GeneratedFile = GeneratedFile(
    "project/build.properties",
    txt.buildProperties(TemplateDependencyInfo.sbtVersion).toString()
  )

  val pluginsSbt: GeneratedFile = GeneratedFile("project/plugins.sbt", CommonObjectTemplate.templateResource("plugins.sbt"))

  val sbtx: GeneratedFile =
    GeneratedFile(sbtxFile, CommonObjectTemplate.templateResource(sbtxFile))

  val README: GeneratedFile =
    GeneratedFile(CommonObjectTemplate.readMePath, CommonObjectTemplate.templateResource("README_sbt.md"))
}

object CommonObjectTemplate {
  val scalafmtConfigPath = ".scalafmt.conf"

  val readMePath: String = "README.md"

  implicit class StarterDetailsWithLegalizedGroupId(starterDetails: StarterDetails) {
    lazy val legalizedGroupId: String = legalizeGroupId(starterDetails)
  }

  private def legalizeGroupId(starterDetails: StarterDetails): String = {
    def legalize(packageNameSection: String): String = {
      val startsWithNumberRgx = "^\\d+.*$"

      if (packageNameSection.matches(startsWithNumberRgx)) {
        "_" + packageNameSection
      } else {
        packageNameSection
      }
    }

    def legalizeGroupId(groupId: String): String = groupId.split('.').map(legalize).mkString(".")

    legalizeGroupId(starterDetails.groupId)
  }

  def templateResource(fileName: String): String = Resource.getAsString(s"template/$fileName")
}

object SbtProjectTemplate {
  val sbtxFile = "sbtx"
}

class ScalaCliProjectTemplate extends ProjectTemplate {
  def getBuildScalaCli(starterDetails: StarterDetails): GeneratedFile = {
    val content = txt
      .scalaCliBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        starterDetails.scalaVersion.value,
        (BuildScalaCliView.getMainDependencies _).andThen(BuildScalaCliView.format)(starterDetails)
      )
      .toString()
    GeneratedFile("build.sc", content)
  }

  def getTestScalaCli(starterDetails: StarterDetails): GeneratedFile = {
    val content = (BuildScalaCliView.getAllTestDependencies _).andThen(BuildScalaCliView.format)(starterDetails)
    GeneratedFile("src/test/scala/test.sc", content)
  }

  lazy val README: GeneratedFile =
    GeneratedFile(CommonObjectTemplate.readMePath, CommonObjectTemplate.templateResource("README_scala-cli.md"))
}
