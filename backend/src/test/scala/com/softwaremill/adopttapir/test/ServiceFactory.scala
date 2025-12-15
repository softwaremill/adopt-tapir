package com.softwaremill.adopttapir.test

import cats.effect.IO
import cats.syntax.show.*
import com.softwaremill.adopttapir.starter.Builder
import com.softwaremill.adopttapir.test.ShowHelpers.throwableShow
import org.scalatest.Assertions
import os.SubProcess

import java.time.LocalDateTime
import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.matching.Regex
import ExecutionContext.Implicits.global

object ServiceTimeouts:
  val waitForScalaCliCompileAndUnitTest: FiniteDuration = 45.seconds
  val waitForPortTimeout: FiniteDuration = 90.seconds
  val readLineTimeout: FiniteDuration = 10.seconds

abstract class GeneratedService:
  import ServiceTimeouts.waitForPortTimeout

  protected val portPattern: Regex
  protected lazy val process: SubProcess

  val port: IO[Integer] =
    val stdOut = new mutable.StringBuilder()
    println(s"[DEBUG port] Starting port detection, timeout=${waitForPortTimeout}")
    IO.blocking {
      val startTime = System.currentTimeMillis()
      val port = waitForPort(stdOut, 0)
      val duration = System.currentTimeMillis() - startTime
      println(s"[DEBUG port] waitForPort returned port=$port after ${duration}ms")
      assert(port > -1)
      port
    }.timeoutAndForget(waitForPortTimeout)
      .onError(e =>
        Assertions.fail(
          s"Detecting port of the running server failed ${
              if e.isInstanceOf[TimeoutException] then s"due to timeout [${waitForPortTimeout}s]"
              else s"Exception:${System.lineSeparator()}${e.show}"
            } with process std output:${System.lineSeparator()}$stdOut"
        )
      )

  @tailrec
  private def waitForPort(stdOut: mutable.StringBuilder, iteration: Int = 0): Integer =
    val stdoutAvailable = process.stdout.available()
    val processAlive = process.isAlive()
    val timestamp = System.currentTimeMillis()

    println(s"[DEBUG waitForPort] iteration=$iteration, available=$stdoutAvailable, alive=$processAlive, time=$timestamp")

    if !processAlive then {
      val exitCode = try {
        Some(process.exitCode())
      } catch {
        case _: Exception => None
      }
      println(s"[DEBUG waitForPort] Process not alive, exitCode=$exitCode, returning -1")
      -1
    } else if process.stdout.available() > 0 || process.isAlive() then {
      println(s"[DEBUG waitForPort] Calling readLine() at iteration $iteration (available=$stdoutAvailable)")
      val readStartTime = System.currentTimeMillis()

      val line = try {
        val lineFuture = Future(process.stdout.readLine())
        Await.result(lineFuture, ServiceTimeouts.readLineTimeout)
      } catch {
        case _: TimeoutException =>
          val readDuration = System.currentTimeMillis() - readStartTime
          val stillAlive = process.isAlive()
          val exitCode = if stillAlive then None else try { Some(process.exitCode()) } catch { case _: Exception => None }
          println(s"[DEBUG waitForPort] readLine() timed out after ${readDuration}ms, processAlive=$stillAlive, exitCode=$exitCode")
          if stillAlive then {
            Thread.sleep(100)
            return waitForPort(stdOut, iteration + 1)
          } else {
            return -1
          }
      }

      val readDuration = System.currentTimeMillis() - readStartTime
      println(s"[DEBUG waitForPort] readLine() returned after ${readDuration}ms, line=${if line == null then "null" else s"length=${line.length}, preview=${line.take(100)}"}")

      if line == null then {
        println(s"[DEBUG waitForPort] readLine() returned null, processAlive=$processAlive")
        -1
      } else {
        stdOut.append("### process log <").append(new Timestamper).append(line).append(">").append(System.lineSeparator())
        val patternMatch = portPattern.findFirstMatchIn(line)
        println(s"[DEBUG waitForPort] Pattern match result: ${if patternMatch.isDefined then s"MATCH port=${patternMatch.get.group(1)}" else "NO MATCH"}")
        patternMatch match {
          case Some(port) => port.group(1).toInt
          case None       => waitForPort(stdOut, iteration + 1)
        }
      }
    } else {
      println(s"[DEBUG waitForPort] Process not alive and no data available, returning -1")
      -1
    }

  def close(): IO[Unit] = IO.blocking {
    if process.isAlive() then process.close()
  }

  private class Timestamper:
    private val timestamp = LocalDateTime.now()

    override def toString: String = s"[$timestamp]"

class ServiceFactory:
  import ServiceTimeouts.waitForScalaCliCompileAndUnitTest

  def create(builder: Builder, tempDir: better.files.File): IO[GeneratedService] =
    builder match {
      case Builder.Sbt      => IO.blocking(SbtService(tempDir))
      case Builder.ScalaCli => IO.blocking(ScalaCliService(tempDir)).timeoutAndForget(waitForScalaCliCompileAndUnitTest)
    }

  private case class SbtService(tempDir: better.files.File) extends GeneratedService:
    override protected val portPattern = new Regex("^(?:\\[info\\] )(?:Go to |Server started at )http://localhost:(\\d+).*")

    override protected lazy val process: SubProcess = {
      os.proc(
        "sbt",
        "-no-colors",
        // start in forked mode so that process input can be forwarded and process waits before closing otherwise `StdIn.readLine` will exit immediately
        "set run / fork := true",
        // forward std input to forked process - https://www.scala-sbt.org/1.x/docs/Forking.html#Configuring+Input
        "set run / connectInput := true",
        ";compile ;test ;run"
      ).spawn(cwd = os.Path(tempDir.toJava), env = Map("HTTP_PORT" -> "0"), mergeErrIntoOut = true)
    }

  private case class ScalaCliService(tempDir: better.files.File) extends GeneratedService:
    override protected val portPattern = new Regex("^(?:Go to |Server started at )http://localhost:(\\d+).*")

    override protected lazy val process: SubProcess =
      // one cannot chain multiple targets to scala-cli hence 'test' target (that implicitly calls compile) is called in
      // blocking manner and once it returns with success (0 exit code) the configuration is actually started
      val compileAndTest = os.proc("scala-cli", "--power", "test", ".").call(cwd = os.Path(tempDir.toJava), mergeErrIntoOut = true)
      assert(
        compileAndTest.exitCode == 0,
        s"Compilation and unit tests exited with [${compileAndTest.exitCode}] and output:${System
            .lineSeparator()}${compileAndTest.out.lines().mkString(System.lineSeparator())}"
      )

      os.proc("scala-cli", "--power", "run", ".")
        .spawn(cwd = os.Path(tempDir.toJava), env = Map("HTTP_PORT" -> "0"), mergeErrIntoOut = true)
