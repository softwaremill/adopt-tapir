package com.softwaremill.adopttapir.starter.content

import cats.effect.IO
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.formatting.ProjectFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

class ContentService(pg: ProjectGenerator, pf: ProjectFormatter) {

  def generateContentTree(starterDetails: StarterDetails): IO[Node] = {
    val projectName = starterDetails.projectName
    val rawGeneratedFiles = pg.generate(starterDetails)

    for {
      formattedGeneratedFiles <- pf.format(rawGeneratedFiles)
      projectAsDirTrees <- IO(formattedGeneratedFiles.map(fgf => {
        val paths = fgf.relativePath.split('/').toList
        val content = fgf.content
        ContentMerger.createTree(projectName, paths, content)
      }))
      projectAsTree <- IO(projectAsDirTrees.reduce(ContentMerger.merge))
    } yield projectAsTree
  }
}
