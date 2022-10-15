package com.softwaremill.adopttapir.http

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader

final case class HttpConfig(host: Host, port: Port, adminPort: Port)

object HttpConfig:
  given ConfigReader[Host] = ConfigReader.fromStringOpt[Host](Host.fromString)
  given ConfigReader[Port] = ConfigReader.fromStringOpt[Port](Port.fromString)
  given ConfigReader[HttpConfig] =
    ConfigReader.forProduct3("host", "port", "admin-port")(HttpConfig(_, _, _))
