package com.softwaremill.adopttapir.starter.api

import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple4Semigroupal, catsSyntaxValidatedId, catsSyntaxValidatedIdBinCompat0}
import com.softwaremill.adopttapir.Fail._
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZIOEffect}
import com.softwaremill.adopttapir.starter.api.RequestValidation.{
  GroupIdShouldFollowJavaPackageConvention,
  NotInSemverNotation,
  ProjectNameShouldMatchRegex
}
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
}

sealed trait FormValidator {
  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(r: StarterRequest): Either[IncorrectInput, StarterDetails] =
    (
      validateSemanticVersioning(r.tapirVersion),
      validateProjectName(r.projectName),
      validateGroupId(r.groupId),
      validateEffectWithImplementation(r.effect, r.implementation)
    ).mapN { case (tapirVersion, projectName, groupId, (effect, serverImplementation)) =>
      StarterDetails(projectName, groupId, effect.toModel, serverImplementation.toModel, tapirVersion, r.addDocumentation)
    }.leftMap(errors => IncorrectInput(errors.toNonEmptyList.map(_.errMessage).toList.mkString(System.lineSeparator())))
      .toEither

  private def validateSemanticVersioning(version: String): ValidationResult[String] = {
    // regex from: https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
    val semverRgx =
      "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    if (version.matches(semverRgx))
      version.valid
    else NotInSemverNotation(version).invalidNec
  }

  private def validateProjectName(projectName: String): ValidationResult[String] = {
    val projectNameRgx = "[a-z0-9]+"

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
}

object FormValidator extends FormValidator
