package com.softwaremill.adopttapir.starter.content

import cats.effect.IO
import com.softwaremill.adopttapir.metrics.Metrics
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.formatting.GeneratedFilesFormatter
import com.softwaremill.adopttapir.template.ProjectGenerator
import cats.syntax.all.*

final case class ContentService(generatedFilesFormatter: GeneratedFilesFormatter)(using Metrics):

  def generateContentTree(starterDetails: StarterDetails): IO[Directory] =
    val projectName = starterDetails.projectName

    for
      rawGeneratedFiles <- ProjectGenerator.generate(starterDetails).liftTo[IO]
      formattedGeneratedFiles <- generatedFilesFormatter.format(rawGeneratedFiles)
      projectAsDirTrees <- formattedGeneratedFiles
        .traverse(fgf => {
          val paths = fgf.relativePath.split('/').toList
          val content = fgf.content
          DirectoryMerger(projectName, paths, content)
        })
        .liftTo[IO]
      projectAsTree <- projectAsDirTrees.toNel match
        case None => IO.raiseError(AssertionError("List of generated files is empty"))
        case Some(nel) =>
          nel.tail.foldLeftM(nel.head)(DirectoryMerger.apply).liftTo[IO]
      _ <- Metrics.increasePreviewOperationMetricCounter(starterDetails)
    yield projectAsTree
