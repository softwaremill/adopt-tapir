package com.softwaremill.adopttapir.starter.content

import cats.effect.IO
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

class ContentService(projectGenerator: ProjectGenerator, generatedFilesFormatter: GeneratedFilesFormatter) {

  def generateContentTree(starterDetails: StarterDetails): IO[Node] = {
    val projectName = starterDetails.projectName
    val rawGeneratedFiles = projectGenerator.generate(starterDetails)

    for {
      formattedGeneratedFiles <- generatedFilesFormatter.format(rawGeneratedFiles)
      projectAsDirTrees <- IO(formattedGeneratedFiles.map(fgf => {
        val paths = fgf.relativePath.split('/').toList
        val content = fgf.content
        DirectoryMerger.createTree(projectName, paths, content)
      }))
      projectAsTree <- IO(projectAsDirTrees.reduce(DirectoryMerger.apply))
    } yield projectAsTree
  }
}
