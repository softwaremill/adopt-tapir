package com.softwaremill.adopttapir.infrastructure

import com.softwaremill.adopttapir.config.Sensitive

case class DBConfig(username: String, password: Sensitive, url: String, migrateOnStart: Boolean, driver: String, connectThreadPoolSize: Int)
