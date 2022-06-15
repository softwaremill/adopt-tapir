package com.softwaremill.adopttapir.http

import com.softwaremill.adopttapir.config.Sensitive

case class HttpConfig(host: String, port: Int, adminPassword: Sensitive)
