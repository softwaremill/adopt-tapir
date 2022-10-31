package com.softwaremill.adopttapir.config

final case class Sensitive(value: String) extends AnyVal:
  override def toString: String = "***"
