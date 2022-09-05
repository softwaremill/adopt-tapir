package com.softwaremill.adopttapir.starter.content

import com.softwaremill.adopttapir.test.BaseTest

class DirectoryMergerTest extends BaseTest {

  object DirectoryMergerTest {
    val TopDirName: String = "top-dir-name"
    val EmptyPathList: List[String] = List()
    val EmptyContent: String = ""
    val NonEmptyContent: String = "Test"

    val FileName: String = "FileName"
    val DirName: String = "DirName"
    val DifferentDirName: String = "DifferentDirName"
    val EmptyNodesList: List[Node] = List()
  }

  import DirectoryMergerTest._

  "createTree" should "throw if `pathsNames` is empty" in {
    an[AssertionError] should be thrownBy DirectoryMerger(
      TopDirName,
      EmptyPathList,
      EmptyContent
    )
  }

  "createTree" should "create single dir / single file structure" in {
    val fileName = "root"
    DirectoryMerger(
      TopDirName,
      List(fileName),
      EmptyContent
    ) should be(Directory(name = TopDirName, content = List(File(name = fileName, content = EmptyContent))))
  }

  "createTree" should "create multiple dirs / single file structure" in {
    val (dirName1, dirName2, dirName3, fileName) = ("dirName1", "dirName2", "dirName3", "fileName")
    DirectoryMerger(
      TopDirName,
      List(dirName1, dirName2, dirName3, fileName),
      NonEmptyContent
    ) should be(
      Directory(
        name = TopDirName,
        content = List(
          Directory(
            name = dirName1,
            content = List(
              Directory(
                name = dirName2,
                content = List(Directory(name = dirName3, content = List(File(name = fileName, content = NonEmptyContent))))
              )
            )
          )
        )
      )
    )
  }

  "merge" should "throw if dirs with different names are about to be merged" in {
    val (dirWithSomeName, dirWithDifferentName) =
      (Directory(name = DirName, EmptyNodesList), Directory(name = DifferentDirName, EmptyNodesList))

    an[AssertionError] should be thrownBy DirectoryMerger(dirWithSomeName, dirWithDifferentName)
  }

  "merge" should "produce empty dir if both dirs to merge are empty" in {
    val (noFilesDir, anotherNoFilesDir) = (Directory(name = DirName, EmptyNodesList), Directory(name = DirName, EmptyNodesList))

    DirectoryMerger(noFilesDir, anotherNoFilesDir) should be(noFilesDir)
  }

  "merge" should "produce dir with one file if one of the dirs to has one file" in {
    val (dirWithFile, dirWithNoFiles) =
      (Directory(name = DirName, List(File(FileName, NonEmptyContent))), Directory(name = DirName, EmptyNodesList))

    DirectoryMerger(dirWithFile, dirWithNoFiles) should be(dirWithFile)
    DirectoryMerger(dirWithNoFiles, dirWithFile) should be(dirWithFile)
  }

  "merge" should "override second file if both files is dirs have the same name" in {
    val (dirWithNonEmptyContentFile, dirWithEmptyContentFile) =
      (Directory(name = DirName, List(File(FileName, NonEmptyContent))), Directory(name = DirName, List(File(FileName, EmptyContent))))

    DirectoryMerger(dirWithNonEmptyContentFile, dirWithEmptyContentFile) should be(dirWithNonEmptyContentFile)
    DirectoryMerger(dirWithEmptyContentFile, dirWithNonEmptyContentFile) should be(dirWithEmptyContentFile)
  }

  "merge" should "create proper project structure when project like files are given" in {
    val projectName = "project-name"
    val files = List(
      DirectoryMerger(projectName, "src/main/scala/group/id/Main.scala".split('/').toList, "package group.id..."),
      DirectoryMerger(projectName, "src/main/scala/group/id/Endpoints.scala".split('/').toList, "package group.id..."),
      DirectoryMerger(projectName, "src/test/scala/group/id/EndpointsSpec.scala".split('/').toList, "package group.id..."),
      DirectoryMerger(projectName, List(".scalafmt.conf"), "version = 3.5.8..."),
      DirectoryMerger(projectName, List("build.sbt"), "val tapirVersion = \"1.0.6\"..."),
      DirectoryMerger(projectName, List("project", "build.properties"), "sbt.version=1.7.1..."),
      DirectoryMerger(projectName, List("project", "plugins.sbt"), "addSbtPlugin(\"org.scalameta\" % \"sbt-scalafmt\" % \"2.4.6\")..."),
      DirectoryMerger(projectName, List("sbtx"), "#!/usr/bin/env bash..."),
      DirectoryMerger(projectName, List("README.md"), "## Quick start...")
    )

    files.reduce(DirectoryMerger.apply) should be(
      Directory(
        projectName,
        List(
          Directory(
            "src",
            List(
              Directory(
                "test",
                List(
                  Directory(
                    "scala",
                    List(Directory("group", List(Directory("id", List(File("EndpointsSpec.scala", "package group.id..."))))))
                  )
                )
              ),
              Directory(
                "main",
                List(
                  Directory(
                    "scala",
                    List(
                      Directory(
                        "group",
                        List(
                          Directory("id", List(File("Main.scala", "package group.id..."), File("Endpoints.scala", "package group.id...")))
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          File("sbtx", "#!/usr/bin/env bash..."),
          Directory(
            "project",
            List(
              File("plugins.sbt", "addSbtPlugin(\"org.scalameta\" % \"sbt-scalafmt\" % \"2.4.6\")..."),
              File("build.properties", "sbt.version=1.7.1...")
            )
          ),
          File("build.sbt", "val tapirVersion = \"1.0.6\"..."),
          File("README.md", "## Quick start..."),
          File(".scalafmt.conf", "version = 3.5.8...")
        )
      )
    )
  }
}
