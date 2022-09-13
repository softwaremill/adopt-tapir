package com.softwaremill.adopttapir.starter.files

import pureconfig.ConfigReader

case class StorageConfig(
    deleteTempFolder: Boolean,
    tempPrefix: String
)

object StorageConfig {
  implicit val storageConfigReader: ConfigReader[StorageConfig] =
    ConfigReader.forProduct2("delete-temp-folder", "temp-prefix")(StorageConfig(_, _))
}
