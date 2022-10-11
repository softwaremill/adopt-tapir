package com.softwaremill.adopttapir.starter.files

import pureconfig.ConfigReader

final case class StorageConfig(
    deleteTempFolder: Boolean,
    tempPrefix: String
)

object StorageConfig:
  given ConfigReader[StorageConfig] =
    ConfigReader.forProduct2("delete-temp-folder", "temp-prefix")(StorageConfig(_, _))

