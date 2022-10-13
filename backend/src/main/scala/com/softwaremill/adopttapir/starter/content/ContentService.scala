package com.softwaremill.adopttapir.starter.content

import cats.effect.IO
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator

final case class ContentService(generatedFilesFormatter: GeneratedFilesFormatter):

  def generateContentTree(starterDetails: StarterDetails): IO[Directory] =
    val projectName = starterDetails.projectName
    val rawGeneratedFiles = ProjectGenerator.generate(starterDetails)

    for
      formattedGeneratedFiles <- generatedFilesFormatter.format(rawGeneratedFiles)
      projectAsDirTrees <- IO(formattedGeneratedFiles.map(fgf => {
        val paths = fgf.relativePath.split('/').toList
        val content = fgf.content
        DirectoryMerger(projectName, paths, content)
      }))
      projectAsTree <- IO(projectAsDirTrees.reduce(DirectoryMerger.apply))
      _ <- IO(Metrics.increasePreviewOperationMetricCounter(starterDetails))
     yield projectAsTree


