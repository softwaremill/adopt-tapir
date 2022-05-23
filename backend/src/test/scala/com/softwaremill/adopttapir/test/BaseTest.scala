package com.softwaremill.adopttapir.test

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers with EitherValues {
  val testClock = new TestClock()
}
