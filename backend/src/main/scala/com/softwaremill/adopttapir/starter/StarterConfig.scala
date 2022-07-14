package com.softwaremill.adopttapir.starter

case class StarterConfig(
    deleteTempFolder: Boolean,
    tempPrefix: String,
    // TODO: In future it will be moved to the request.
    scalaVersion: String
)
