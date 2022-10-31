package com.softwaremill.adopttapir.http

import com.comcast.ip4s.{Host, Port}
import pureconfig.generic.derivation.default.*
import pureconfig.ConfigReader

final case class HttpConfig(host: Host, port: Port, adminPort: Port) derives ConfigReader

object HttpConfig:
  given ConfigReader[Host] = ConfigReader.fromStringOpt[Host](Host.fromString)
  given ConfigReader[Port] = ConfigReader.fromStringOpt[Port](Port.fromString)
