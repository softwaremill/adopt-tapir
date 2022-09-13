package com.softwaremill.adopttapir.http

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader

final case class HttpConfig(host: Host, port: Port, adminPort: Port)

object HttpConfig {
  implicit val hostReader: ConfigReader[Host] = ConfigReader.fromStringOpt[Host](Host.fromString)
  implicit val portReader: ConfigReader[Port] = ConfigReader.fromStringOpt[Port](Port.fromString)
  implicit val httpConfigReader: ConfigReader[HttpConfig] =
    ConfigReader.forProduct3("host", "port", "admin-port")(HttpConfig(_, _, _))

}
