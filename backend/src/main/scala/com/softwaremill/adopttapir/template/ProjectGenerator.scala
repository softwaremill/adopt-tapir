package com.softwaremill.adopttapir.template

import better.files.Resource
import com.softwaremill.adopttapir.starter.ServerEffect.ZIOEffect
import com.softwaremill.adopttapir.starter.{Builder, ScalaVersion, StarterDetails}
import com.softwaremill.adopttapir.template.scala.{EndpointsSpecView, EndpointsView, Import, MainView}
import com.softwaremill.adopttapir.version.TemplateDependencyInfo
import cats.syntax.all.*

final case class GeneratedFile(
    relativePath: String,
    content: String
)

object ProjectGenerator:
  def generate(starterDetails: StarterDetails): Either[UnsupportedOperationException, List[GeneratedFile]] =
    starterDetails.builder match
      case Builder.Sbt      => SbtProjectTemplate.generate(starterDetails)
      case Builder.ScalaCli => ScalaCliProjectTemplate.generate(starterDetails)

/** Twirl library was chosen for templating. Due to limitations in Twirl, some of arguments are passed as [[String]].<br> More advanced
  * rendering is done by dedicated objects `*View` e.g. @see [[EndpointsView]] or @[[MainView]].
  */
abstract class ProjectTemplate:

  import CommonObjectTemplate.StarterDetailsWithLegalizedGroupId

  def generate(starterDetails: StarterDetails): Either[UnsupportedOperationException, List[GeneratedFile]] = {
    // common project elements regardless of the builder type
    getMain(starterDetails).map(main =>
      List(
        main,
        getEndpoints(starterDetails),
        getEndpointsSpec(starterDetails),
        scalafmtConf(starterDetails.scalaVersion)
      )
    )

  }

  private def getMain(starterDetails: StarterDetails): Either[UnsupportedOperationException, GeneratedFile] = {
    val groupId = starterDetails.legalizedGroupId

    MainView
      .getProperMainContent(starterDetails)
      .map(content =>
        GeneratedFile(
          pathUnderPackage("src/main/scala", groupId, "Main.scala"),
          content
        )
      )
  }

  private def getEndpoints(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    val helloServerEndpoint = EndpointsView.getHelloServerEndpoint(starterDetails)
    val jsonEndpoint = EndpointsView.getJsonOutModel(starterDetails)
    val library = EndpointsView.getJsonLibrary(starterDetails)
    val apiEndpoints = EndpointsView.getApiEndpoints(starterDetails)
    val docEndpoints = EndpointsView.getDocEndpoints(starterDetails)
    val metricsEndpoint = EndpointsView.getMetricsEndpoint(starterDetails)
    val allEndpoints = EndpointsView.getAllEndpoints(starterDetails)

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Endpoints.scala"),
      txt
        .Endpoints(
          starterDetails = starterDetails,
          additionalImports = toSortedList(
            helloServerEndpoint.imports ++ metricsEndpoint.imports ++ docEndpoints.imports
              ++ jsonEndpoint.imports ++ library.imports ++ allEndpoints.imports
          ),
          helloEndpointServer = helloServerEndpoint.body,
          jsonEndpoint = jsonEndpoint.body,
          library = library.body,
          apiEndpoints = apiEndpoints.body,
          docEndpoints = docEndpoints.body,
          metricsEndpoint = metricsEndpoint.body,
          allEndpoints = allEndpoints.body,
          scalaVersion = starterDetails.scalaVersion
        )
        .toString()
    )
  }

  private def getEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    val helloServerStub = EndpointsSpecView.getHelloServerStub(starterDetails)
    val booksServerStub = EndpointsSpecView.getBookServerStub(starterDetails)

    val fileContent =
      if starterDetails.serverEffect == ZIOEffect then {
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

  import CommonObjectTemplate.scalafmtConfigPath

  private val scalafmtConf: ScalaVersion => GeneratedFile = dialectVersion =>
    GeneratedFile(scalafmtConfigPath, txt.scalafmt(TemplateDependencyInfo.scalafmtVersion, dialectVersion).toString())

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName

  private def toSortedList(set: Set[Import]): List[Import] = set.toList.sortBy(_.fullName)

end ProjectTemplate

object SbtProjectTemplate extends ProjectTemplate:
  override def generate(starterDetails: StarterDetails): Either[UnsupportedOperationException, List[GeneratedFile]] =
    for
      common <- super.generate(starterDetails)
      build <- getBuildSbt(starterDetails)
    yield common ::: List(build, buildProperties, pluginsSbt, sbtx, readme, gitignore)

  lazy val sbtxFile = "sbtx"

  private def getBuildSbt(starterDetails: StarterDetails): Either[UnsupportedOperationException, GeneratedFile] = {
    BuildSbtView
      .getAllDependencies(starterDetails)
      .map(BuildSbtView.format)
      .map(dependencies =>
        val content = txt
          .sbtBuild(
            starterDetails.projectName,
            starterDetails.groupId,
            starterDetails.scalaVersion.value,
            TemplateDependencyInfo.tapirVersion,
            dependencies,
            starterDetails.serverEffect == ZIOEffect
          )
          .toString()

        GeneratedFile("build.sbt", content)
      )
  }

  private lazy val buildProperties: GeneratedFile = GeneratedFile(
    "project/build.properties",
    txt.buildProperties(TemplateDependencyInfo.sbtVersion).toString()
  )

  private lazy val pluginsSbt: GeneratedFile = GeneratedFile("project/plugins.sbt", CommonObjectTemplate.templateResource("plugins.sbt"))

  private lazy val sbtx: GeneratedFile =
    GeneratedFile(sbtxFile, CommonObjectTemplate.templateResource(sbtxFile))

  private lazy val readme: GeneratedFile =
    GeneratedFile(CommonObjectTemplate.readMePath, CommonObjectTemplate.templateResource("README_sbt.md"))

  private lazy val gitignore: GeneratedFile =
    GeneratedFile(".gitignore", txt.gitignore(List(".bloop", "target", "metals.sbt", "project/project")).toString())

end SbtProjectTemplate

object CommonObjectTemplate:
  def templateResource(fileName: String): String = Resource.getAsString(s"template/$fileName")

  val scalafmtConfigPath = ".scalafmt.conf"

  val readMePath: String = "README.md"

  implicit class StarterDetailsWithLegalizedGroupId(starterDetails: StarterDetails) {
    lazy val legalizedGroupId: String = legalizeGroupId(starterDetails)
  }

  private def legalizeGroupId(starterDetails: StarterDetails): String = {
    def legalize(packageNameSection: String): String = {
      val startsWithNumberRgx = "^\\d+.*$"

      if packageNameSection.matches(startsWithNumberRgx) then {
        "_" + packageNameSection
      } else {
        packageNameSection
      }
    }

    def legalizeGroupId(groupId: String): String = groupId.split('.').map(legalize).mkString(".")

    legalizeGroupId(starterDetails.groupId)
  }

end CommonObjectTemplate

private object ScalaCliProjectTemplate extends ProjectTemplate:
  override def generate(starterDetails: StarterDetails): Either[UnsupportedOperationException, List[GeneratedFile]] =
    for
      common <- super.generate(starterDetails)
      build <- getBuildScalaCli(starterDetails)
    yield common ::: List(build, getTestScalaCli(starterDetails), readme, gitignore)

  private def getBuildScalaCli(starterDetails: StarterDetails): Either[UnsupportedOperationException, GeneratedFile] = {
    BuildScalaCliView
      .getMainDependencies(starterDetails)
      .map(BuildScalaCliView.format)
      .map(dependencies =>
        val content = txt
          .scalaCliBuild(
            starterDetails.projectName,
            starterDetails.groupId,
            starterDetails.scalaVersion.value,
            dependencies
          )
          .toString()
        GeneratedFile("build.scala", content)
      )

  }

  private def getTestScalaCli(starterDetails: StarterDetails): GeneratedFile = {
    val content = (BuildScalaCliView.getAllTestDependencies _).andThen(BuildScalaCliView.format)(starterDetails)
    GeneratedFile("src/test/scala/build.test.scala", content)
  }

  private lazy val readme: GeneratedFile =
    GeneratedFile(CommonObjectTemplate.readMePath, CommonObjectTemplate.templateResource("README_scala-cli.md"))

  private lazy val gitignore: GeneratedFile = GeneratedFile(".gitignore", txt.gitignore(List(".bsp/", ".scala-build/")).toString())

end ScalaCliProjectTemplate
