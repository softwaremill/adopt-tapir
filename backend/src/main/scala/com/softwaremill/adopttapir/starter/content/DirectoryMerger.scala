package com.softwaremill.adopttapir.starter.content

object DirectoryMerger {

  def createTree(topDirName: String, pathNames: List[String], content: String): Directory = {
    assert(pathNames.nonEmpty, "`pathNames` list cannot be empty!")

    def innerCreateTree(pathNames: List[String], content: String): Node = {
      pathNames match {
        case Nil          => throw new IllegalStateException("pathNames list cannot be empty!")
        case last :: Nil  => File(last, content)
        case head :: tail => Directory(head, List(innerCreateTree(tail, content)))
      }
    }

    val pathsWithRootDir = List(topDirName) ++ pathNames
    innerCreateTree(pathsWithRootDir, content) match {
      case File(_, _)          => throw new IllegalStateException("Top element of created tree cannot be a `File`!")
      case d @ Directory(_, _) => d
    }
  }

  def apply(d1: Directory, d2: Directory): Directory = {
    assert(d1.name == d2.name, "names of directories to merge cannot be different!")

    val (d1DirsAll, d2DirsAll) = (d1.childDirectories(), d2.childDirectories())
    val dirsIntersection = for {
      d1 <- d1DirsAll
      d2 <- d2DirsAll
      if d1.name == d2.name
    } yield (d1, d2)
    val mergedDirs = dirsIntersection.map(d1d2 => DirectoryMerger(d1d2._1, d1d2._2))
    val d1DirsUnique = d1DirsAll.filter(d => !mergedDirs.exists(md => md.name == d.name))
    val d2DirsUnique = d2DirsAll.filter(d => !mergedDirs.exists(md => md.name == d.name))

    val (d1FilesAll, d2FilesAll) = (d1.childFiles(), d2.childFiles())
    // top level files cannot be merged, dropping files from `d2`
    val filesIntersection = d1FilesAll.filter(f1 => d2FilesAll.exists(f2 => f2.name == f1.name))
    val d1FilesUnique = d1FilesAll.filter(f => !filesIntersection.exists(fi => fi.name == f.name))
    val d2FilesUnique = d2FilesAll.filter(f => !filesIntersection.exists(fi => fi.name == f.name))

    Directory(
      name = d1.name,
      content = (filesIntersection ++ d1FilesUnique ++ d2FilesUnique ++ mergedDirs ++ d1DirsUnique ++ d2DirsUnique)
        .sortWith((n1, n2) => n1.name.compareTo(n2.name) > 0)
    )
  }
}
