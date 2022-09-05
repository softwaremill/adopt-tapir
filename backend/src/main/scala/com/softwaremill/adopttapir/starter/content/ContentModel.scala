package com.softwaremill.adopttapir.starter.content

sealed trait Node {
  def name: String
}

case class File(name: String, content: String) extends Node
case class Directory(name: String, content: List[Node]) extends Node {
  def childFiles(): List[File] = content.collect { case f: File => f }
  def childDirectories(): List[Directory] = content.collect { case d: Directory => d }
}
