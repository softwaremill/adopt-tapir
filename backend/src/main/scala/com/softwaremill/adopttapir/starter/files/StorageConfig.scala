package com.softwaremill.adopttapir.starter.files

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class StorageConfig(deleteTempFolder: Boolean, tempPrefix: String) derives ConfigReader
