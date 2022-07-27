package com.softwaremill.adopttapir.template

import better.files.Resource
import com.softwaremill.adopttapir.starter.ServerEffect.ZIOEffect
import com.softwaremill.adopttapir.starter.{ScalaVersion, StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectTemplate._
import com.softwaremill.adopttapir.template.sbt.BuildSbtView
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
class ProjectTemplate(config: StarterConfig) {

  def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {
    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        starterDetails.scalaVersion.value,
        TemplateDependencyInfo.tapirVersion,
        (BuildSbtView.getDependencies _).andThen(BuildSbtView.format)(starterDetails),
        starterDetails.serverEffect == ZIOEffect
      )
      .toString()

    GeneratedFile("build.sbt", content)
  }

  def getBuildProperties: GeneratedFile = GeneratedFile(
    "project/build.properties",
    txt.buildProperties(TemplateDependencyInfo.sbtVersion).toString()
  )

  def getMain(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Main.scala"),
      MainView.getProperMainContent(starterDetails)
    )
  }

  def getEndpoints(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

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
          allEndpoints.body
        )
        .toString()
    )
  }

  def getEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val helloServerStub = EndpointsSpecView.getHelloServerStub(starterDetails)
    val booksServerStub = EndpointsSpecView.getBookServerStub(starterDetails)

    val fileContent =
      if (starterDetails.serverEffect == ZIOEffect) {
        txt
          .EndpointsSpecZIO(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports),
            helloServerStub.body,
            booksServerStub.body
          )
      } else {
        val unwrapper = EndpointsSpecView.Unwrapper.prepareUnwrapper(starterDetails.serverEffect)
        txt
          .EndpointsSpec(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports ++ unwrapper.imports),
            helloServerStub.body,
            unwrapper.body,
            booksServerStub.body
          )
      }

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "EndpointsSpec.scala"),
      fileContent.toString
    )
  }

  val pluginsSbt: GeneratedFile = GeneratedFile("project/plugins.sbt", templateResource("plugins.sbt"))

  val scalafmtConf: ScalaVersion => GeneratedFile = dialectVersion =>
    GeneratedFile(ScalafmtConfigFile, txt.scalafmt(TemplateDependencyInfo.scalafmtVersion, dialectVersion).toString())

  val sbtx: GeneratedFile =
    GeneratedFile(sbtxFile, templateResource(sbtxFile))
  val README: GeneratedFile =
    GeneratedFile(readMeFile, templateResource(readMeFile))

  protected def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName

  private def templateResource(fileName: String): String = Resource.getAsString(s"template/$fileName")
}

object ProjectTemplate {
  val ScalafmtConfigFile = ".scalafmt.conf"
  val sbtxFile = "sbtx"
  val readMeFile = "README.md"

  def legalizeGroupId(starterDetails: StarterDetails): StarterDetails = {
    def legalize(packageNameSection: String): String = {
      val startsWithNumberRgx = "^\\d+.*$"

      if (packageNameSection.matches(startsWithNumberRgx)) {
        "_" + packageNameSection
      } else {
        packageNameSection
      }
    }

    def legalizeGroupId(groupId: String): String = groupId.split('.').map(legalize).mkString(".")

    starterDetails.copy(groupId = legalizeGroupId(starterDetails.groupId))
  }

  def toSortedList(set: Set[Import]): List[Import] = set.toList.sortBy(_.fullName)
}
