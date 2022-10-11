package com.softwaremill.adopttapir.config

case class Sensitive(value: String) extends AnyVal:
  override def toString: String = "***"
