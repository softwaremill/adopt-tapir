package com.softwaremill.adopttapir.starter.content

import com.softwaremill.adopttapir.logging.FLogging
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.template.ProjectGenerator

class ContentService(projectGenerator: ProjectGenerator) extends FLogging {

  def generateContentTree(starterDetails: StarterDetails): Node = {
    val projectName = starterDetails.projectName
    val projectAsFilesWithPaths = projectGenerator.generate(starterDetails)
    val projectAsDirTrees = projectAsFilesWithPaths.map(gf => {
      val pathsWithRootDir = (projectName + "/" + gf.relativePath).split('/').toList
      val content = gf.content
      createTree(pathsWithRootDir, content)
    }).map(_.asInstanceOf[Directory])
    val projectAsTree = projectAsDirTrees.reduce(merge)
    projectAsTree
  }

  private def createTree(pathNames: List[String], content: String): Node = {
    pathNames match {
      case Nil => throw new IllegalStateException("pathNames list cannot be empty!")
      case last :: Nil => File(last, content)
      case head :: tail => Directory(head, List(createTree(tail, content)))
    }
  }

  private def merge(n1: Directory, n2: Directory): Directory = {
    val files = n1.content.filter(_.isInstanceOf[File]) ++ n2.content.filter(_.isInstanceOf[File])
    val n1Dirs = n1.content.filter(_.isInstanceOf[Directory]).map(_.asInstanceOf[Directory])
    val n1DirsNames = n1Dirs.map(_.name)
    val n2Dirs = n2.content.filter(_.isInstanceOf[Directory]).map(_.asInstanceOf[Directory])
    val n2DirsNames = n2Dirs.map(_.name)

    val n1UniqueDirs = n1Dirs.filter(n1d => !n2DirsNames.contains(n1d.name))
    val n2UniqueDirs = n2Dirs.filter(n2d => !n1DirsNames.contains(n2d.name))
    val uniqueDirs = n1UniqueDirs ++ n2UniqueDirs
    val n1duplicatedDirs = n1Dirs.filter(n1d => n2DirsNames.contains(n1d.name))
    val n2duplicatedDirs = n2Dirs.filter(n2d => n1DirsNames.contains(n2d.name))

    val mergedDirs = n1duplicatedDirs.map(dd => {
      merge(dd, n2duplicatedDirs.find(_.name == dd.name).get)
    })

    Directory(
      name = n1.name,
      content = (files ++ uniqueDirs ++ mergedDirs).sortWith((n1, n2) => n1.name.compareTo(n2.name) > 0)
    )
  }
}
