package com.softwaremill.adopttapir.template

import better.files.Resource
import com.softwaremill.adopttapir.starter.ServerStack.{OxStack, ZIOStack}
import com.softwaremill.adopttapir.starter.{Builder, ScalaVersion, StarterDetails}
import com.softwaremill.adopttapir.template.scala.{EndpointsSpecView, EndpointsView, Import, MainView}
import com.softwaremill.adopttapir.version.TemplateDependencyInfo

final case class GeneratedFile(
    relativePath: String,
    content: String
)

object ProjectGenerator:
  def generate(starterDetails: StarterDetails): List[GeneratedFile] =
    starterDetails.builder match
      case Builder.Sbt                => SbtProjectTemplate.generate(starterDetails)
      case Builder.ScalaCli           => ScalaCliProjectTemplate.generate(starterDetails)
      case Builder.ScalaCliSingleFile => ScalaCliSingleFileTemplate.generate(starterDetails)

/** Twirl library was chosen for templating. Due to limitations in Twirl, some of arguments are passed as [[String]].<br> More advanced
  * rendering is done by dedicated objects `*View` e.g. @see [[EndpointsView]] or @[[MainView]].
  */
abstract class ProjectTemplate:

  import CommonObjectTemplate.StarterDetailsWithLegalizedGroupId

  def generate(starterDetails: StarterDetails): List[GeneratedFile] = {
    // common project elements regardless of the builder type
    List(
      getMain(starterDetails),
      getEndpoints(starterDetails),
      getEndpointsSpec(starterDetails),
      scalafmtConf(starterDetails.scalaVersion)
    ) ++ getLogback()
  }

  private def getMain(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.legalizedGroupId

    GeneratedFile(
      pathUnderPackage("src/main/scala", groupId, "Main.scala"),
      MainView.getProperMainContent(starterDetails)
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
      if starterDetails.serverStack == ZIOStack then {
        txt
          .EndpointsSpecZIO(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports),
            helloServerStub.body,
            booksServerStub.body,
            starterDetails.scalaVersion,
            starterDetails.jsonImplementation
          )
      } else if starterDetails.serverStack == OxStack then {
        txt
          .EndpointsSpecSync(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports),
            helloServerStub.body,
            booksServerStub.body,
            starterDetails.scalaVersion,
            starterDetails.jsonImplementation
          )
      } else {
        val unwrapper = EndpointsSpecView.Unwrapper.prepareUnwrapper(starterDetails.serverStack, starterDetails.scalaVersion)
        txt
          .EndpointsSpec(
            starterDetails,
            toSortedList(helloServerStub.imports ++ booksServerStub.imports ++ unwrapper.imports),
            helloServerStub.body,
            unwrapper.body,
            booksServerStub.body,
            starterDetails.scalaVersion,
            starterDetails.jsonImplementation
          )
      }

    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "EndpointsSpec.scala"),
      fileContent.toString
    )
  }

  private def getLogback(): List[GeneratedFile] =
    List(
      GeneratedFile("src/main/resources/logback.xml", txt.logback().toString)
    )

  import CommonObjectTemplate.scalafmtConfigPath

  private val scalafmtConf: ScalaVersion => GeneratedFile = dialectVersion =>
    GeneratedFile(scalafmtConfigPath, txt.scalafmt(TemplateDependencyInfo.scalafmtVersion, dialectVersion).toString())

  private def pathUnderPackage(prefixDir: String, groupId: String, fileName: String): String =
    prefixDir + "/" + groupId.split('.').mkString("/") + "/" + fileName

  private def toSortedList(set: Set[Import]): List[Import] = set.toList.sortBy(_.fullName)

end ProjectTemplate

object SbtProjectTemplate extends ProjectTemplate:
  override def generate(starterDetails: StarterDetails): List[GeneratedFile] =
    super.generate(starterDetails) ::: List(getBuildSbt(starterDetails), buildProperties, pluginsSbt, sbtx, readme, gitignore)

  lazy val sbtxFile = "sbtx"

  private def getBuildSbt(starterDetails: StarterDetails): GeneratedFile = {
    val content = txt
      .sbtBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        starterDetails.scalaVersion.value,
        TemplateDependencyInfo.tapirVersion,
        (BuildSbtView.getAllDependencies _).andThen(BuildSbtView.format)(starterDetails),
        starterDetails.serverStack == ZIOStack
      )
      .toString()

    GeneratedFile("build.sbt", content)
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
  override def generate(starterDetails: StarterDetails): List[GeneratedFile] =
    super.generate(starterDetails) ::: List(getProjectScalaCli(starterDetails), readme, gitignore)

  private def getProjectScalaCli(starterDetails: StarterDetails): GeneratedFile = {
    val dependencies = (BuildScalaCliView.getMainDependencies _).andThen(deps => BuildScalaCliView.format(deps, false))(starterDetails) +
      (BuildScalaCliView.getAllTestDependencies _).andThen(deps => BuildScalaCliView.format(deps, true))(starterDetails)

    val content = txt
      .scalaCliBuild(
        starterDetails.projectName,
        starterDetails.groupId,
        starterDetails.scalaVersion.value,
        dependencies
      )
      .toString()
    GeneratedFile("project.scala", content)
  }

  private lazy val readme: GeneratedFile =
    GeneratedFile(CommonObjectTemplate.readMePath, CommonObjectTemplate.templateResource("README_scala-cli.md"))

  private lazy val gitignore: GeneratedFile = GeneratedFile(".gitignore", txt.gitignore(List(".bsp/", ".scala-build/")).toString())

end ScalaCliProjectTemplate

private object ScalaCliSingleFileTemplate:
  def generate(starterDetails: StarterDetails): List[GeneratedFile] =
    List(getSingleFile(starterDetails))

  private def getSingleFile(starterDetails: StarterDetails): GeneratedFile = {
    import CommonObjectTemplate.StarterDetailsWithLegalizedGroupId
    val groupId = starterDetails.legalizedGroupId

    val dependencies = (BuildScalaCliView.getMainDependencies _).andThen(deps => BuildScalaCliView.format(deps, false))(starterDetails)

    val helloServerEndpoint = EndpointsView.getHelloServerEndpoint(starterDetails)
    val jsonEndpoint = EndpointsView.getJsonOutModel(starterDetails)
    val library = EndpointsView.getJsonLibrary(starterDetails)
    val apiEndpoints = EndpointsView.getApiEndpoints(starterDetails)
    val docEndpoints = EndpointsView.getDocEndpoints(starterDetails)
    val metricsEndpoint = EndpointsView.getMetricsEndpoint(starterDetails)
    val allEndpoints = EndpointsView.getAllEndpoints(starterDetails)
    val mainContentRaw = MainView.getProperMainContent(starterDetails)
    // Remove package declaration from mainContent since we already have it in the template
    val mainContent = mainContentRaw.linesIterator
      .dropWhile(line => line.trim.startsWith("package"))
      .mkString(System.lineSeparator())

    val allImports = toSortedList(
      helloServerEndpoint.imports ++ metricsEndpoint.imports ++ docEndpoints.imports
        ++ jsonEndpoint.imports ++ library.imports ++ allEndpoints.imports
    )

    val content = txt
      .scalaCliSingleFile(
        starterDetails.projectName,
        groupId,
        starterDetails.scalaVersion.value,
        dependencies,
        allImports,
        helloServerEndpoint.body,
        jsonEndpoint.body,
        library.body,
        apiEndpoints.body,
        docEndpoints.body,
        metricsEndpoint.body,
        allEndpoints.body,
        mainContent,
        starterDetails.scalaVersion
      )
      .toString()

    GeneratedFile(s"${starterDetails.projectName}.scala", content)
  }

  private def toSortedList(set: Set[Import]): List[Import] = set.toList.sortBy(_.fullName)

end ScalaCliSingleFileTemplate
