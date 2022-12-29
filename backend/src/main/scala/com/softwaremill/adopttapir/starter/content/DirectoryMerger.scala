package com.softwaremill.adopttapir.starter.content

import cats.syntax.all.*

object DirectoryMerger:

  def apply(topDirName: String, pathNames: List[String], content: String): Either[IllegalStateException, Directory] = for
    _ <- Either
      .cond(pathNames.nonEmpty, (), IllegalStateException("`pathNames` list cannot be empty!"))
    pathsWithRootDir = List(topDirName) ++ pathNames
    directory <- createTreeRec(pathsWithRootDir, content).flatMap {
      case File(_, _)          => IllegalStateException("Top element of created tree cannot be a `File`!").asLeft
      case d @ Directory(_, _) => d.asRight
    }
  yield directory

  private def createTreeRec(pathNames: List[String], content: String): Either[IllegalStateException, Node] =
    pathNames match {
      case Nil          => IllegalStateException("pathNames list cannot be empty!").asLeft
      case last :: Nil  => File(last, content).asRight
      case head :: tail => createTreeRec(tail, content).map(c => Directory(head, List(c)))
    }

  def apply(d1: Directory, d2: Directory): Either[AssertionError, Directory] = for
    _ <- Either.cond(d1.name == d2.name, (), AssertionError("names of directories to merge cannot be different!"))
    (d1DirsAll, d2DirsAll) = (d1.childDirectories(), d2.childDirectories())
    dirsIntersection = for
      d1 <- d1DirsAll
      d2 <- d2DirsAll if d1.name == d2.name
    yield (d1, d2)
    mergedDirs <- dirsIntersection.traverse(d1d2 => DirectoryMerger(d1d2._1, d1d2._2))
    d1DirsUnique = d1DirsAll.filter(d => !mergedDirs.exists(md => md.name == d.name))
    d2DirsUnique = d2DirsAll.filter(d => !mergedDirs.exists(md => md.name == d.name))

    (d1FilesAll, d2FilesAll) = (d1.childFiles(), d2.childFiles())
    // top level files cannot be merged, dropping files from `d2`
    filesIntersection = d1FilesAll.filter(f1 => d2FilesAll.exists(f2 => f2.name == f1.name))
    d1FilesUnique = d1FilesAll.filter(f => !filesIntersection.exists(fi => fi.name == f.name))
    d2FilesUnique = d2FilesAll.filter(f => !filesIntersection.exists(fi => fi.name == f.name))
  yield Directory(
    name = d1.name,
    content = (filesIntersection ++ d1FilesUnique ++ d2FilesUnique ++ mergedDirs ++ d1DirsUnique ++ d2DirsUnique)
      .sortWith((n1, n2) => n1.name.compareTo(n2.name) > 0)
  )
