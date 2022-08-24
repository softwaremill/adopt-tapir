package com.softwaremill.adopttapir.starter.api

import cats.data.Validated.validNec
import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple6Semigroupal, catsSyntaxValidatedIdBinCompat0}
import com.softwaremill.adopttapir.Fail._
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.ZIOJson
import com.softwaremill.adopttapir.starter.api.RequestValidation.{GroupIdShouldFollowJavaPackageConvention, ProjectNameShouldMatchRegex}
import com.softwaremill.adopttapir.starter.api.ScalaVersionRequest.Scala3
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZIOHttp}

sealed trait RequestValidation {
  def errMessage: String
}

object RequestValidation {
  case class NotInSemverNotation(input: String) extends RequestValidation {
    override val errMessage: String = s"Provided input: `$input` is not in semantic versioning notation"
  }

  case class ProjectNameShouldBeLowerCaseWritten(input: String) extends RequestValidation {
    override val errMessage: String = s"Project name: `$input` should be written with lowercase"
  }

  case class ProjectNameShouldMatchRegex(input: String, regex: String) extends RequestValidation {
    override val errMessage: String = s"Project name: `$input` should match regex: `$regex`"
  }

  case class GroupIdShouldFollowJavaPackageConvention(input: String) extends RequestValidation {
    override val errMessage: String = s"GroupId: `$input` should follow Java package convention and be smaller than 256 characters"
  }

  abstract class EffectValidation {
    val effect: EffectRequest
    val implementation: ServerImplementationRequest
    protected val prefixMessage = s"Picked $effect with $implementation -"
  }

  case class FutureEffectWillWorkOnlyWithAkkaAndNetty(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation {
    override val errMessage: String = s"$prefixMessage Future effect will work only with Akka and Netty"
  }

  case class IOEffectWillWorkOnlyWithHttp4sAndNetty(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation {
    override val errMessage: String = s"$prefixMessage IO effect will work only with Http4 and Netty"
  }

  case class ZIOEffectWillWorkOnlyWithHttp4sAndZIOHttp(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation {
    override val errMessage: String = s"$prefixMessage ZIO effect will work only with Http4s and ZIOHttp"
  }

  case class MetricsNotSupportedForNettyServerImplementation(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation {
    override val errMessage: String = s"$prefixMessage Metrics not supported for ${ServerImplementationRequest.Netty} server implementation"
  }

  case object ZIOJsonWillWorkOnlyWithZIOEffect extends RequestValidation {
    override val errMessage: String = s"ZIOJson will work only with ZIO effect"
  }

  case object AkkaImplementationIsNotSupportedWithScala3Project extends RequestValidation {
    override def errMessage: String = s"$Scala3 version is not supported for $Akka server implementation"
  }
}

sealed trait FormValidator {
  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(r: StarterRequest): Either[IncorrectInput, StarterDetails] =
    (
      validateProjectName(r.projectName),
      validateGroupId(r.groupId),
      validateEffectWithImplementation(r.effect, r.implementation),
      validateMetrics(r.effect, r.implementation, r.addMetrics),
      validateEffectWithJson(r.effect, r.json),
      validateScalaVersion(r.scalaVersion, r.implementation)
    ).mapN { case (projectName, groupId, (effect, serverImplementation), addMetrics, json, scalaVersion) =>
      StarterDetails(
        projectName,
        groupId,
        effect.toModel,
        serverImplementation.toModel,
        r.addDocumentation,
        addMetrics,
        json.toModel,
        scalaVersion.toModel,
        r.builder.toModel
      )
    }.leftMap(errors => IncorrectInput(errors.toNonEmptyList.map(_.errMessage).toList.mkString(System.lineSeparator())))
      .toEither

  private def validateProjectName(projectName: String): ValidationResult[String] = {
    val projectNameRgx = "^[a-z0-9_]$|^[a-z0-9_]+[a-z0-9_-]*[a-z0-9_]+$"

    if (projectName.matches(projectNameRgx))
      projectName.validNec
    else ProjectNameShouldMatchRegex(projectName, projectNameRgx).invalidNec
  }

  private def validateGroupId(groupId: String): ValidationResult[String] = {
    if (groupId.matches("(?:^[a-z][a-z0-9_]*|[a-z][a-z0-9_]*\\.[a-z0-9_]+)+$") && groupId.length <= 256) groupId.validNec
    else GroupIdShouldFollowJavaPackageConvention(groupId).invalidNec
  }

  private def validateEffectWithImplementation(
      effect: EffectRequest,
      serverImplementation: ServerImplementationRequest
  ): ValidatedNec[RequestValidation, (EffectRequest, ServerImplementationRequest)] = {
    (effect, serverImplementation) match {
      case t @ (FutureEffect, Akka)  => t.validNec
      case t @ (FutureEffect, Netty) => t.validNec
      case (FutureEffect, _)         => RequestValidation.FutureEffectWillWorkOnlyWithAkkaAndNetty(effect, serverImplementation).invalidNec
      case t @ (IOEffect, Http4s)    => t.validNec
      case t @ (IOEffect, Netty)     => t.validNec
      case (IOEffect, _)             => RequestValidation.IOEffectWillWorkOnlyWithHttp4sAndNetty(effect, serverImplementation).invalidNec
      case t @ (ZIOEffect, Http4s)   => t.validNec
      case t @ (ZIOEffect, ZIOHttp)  => t.validNec
      case (ZIOEffect, _)            => RequestValidation.ZIOEffectWillWorkOnlyWithHttp4sAndZIOHttp(effect, serverImplementation).invalidNec
    }
  }

  private def validateMetrics(
      effect: EffectRequest,
      serverImplementation: ServerImplementationRequest,
      addMetrics: Boolean
  ): ValidatedNec[RequestValidation, Boolean] = {
    (effect, serverImplementation, addMetrics) match {
      case t @ (_, Akka | Http4s | ZIOHttp | Netty, _) => t._3.validNec
    }
  }

  private def validateEffectWithJson(
      effectRequest: EffectRequest,
      json: JsonImplementationRequest
  ): ValidatedNec[RequestValidation, JsonImplementationRequest] = {
    (effectRequest, json) match {
      case t @ (ZIOEffect, ZIOJson) => t._2.validNec
      case (_, ZIOJson)             => RequestValidation.ZIOJsonWillWorkOnlyWithZIOEffect.invalidNec
      case t                        => t._2.validNec
    }
  }

  private def validateScalaVersion(
      scalaVersion: ScalaVersionRequest,
      implementation: ServerImplementationRequest
  ): ValidatedNec[RequestValidation, ScalaVersionRequest] = {
    (implementation, scalaVersion) match {
      case (Akka, Scala3)    => RequestValidation.AkkaImplementationIsNotSupportedWithScala3Project.invalidNec
      case (_, scalaVersion) => scalaVersion.validNec
    }
  }
}

object FormValidator extends FormValidator
