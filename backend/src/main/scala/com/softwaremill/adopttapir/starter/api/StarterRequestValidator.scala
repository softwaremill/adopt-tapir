package com.softwaremill.adopttapir.starter.api

import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxValidatedIdBinCompat0}
import com.softwaremill.adopttapir.Fail._
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.StarterDetails.{FutureStarterDetails, IOStarterDetails, ZIOStarterDetails, defaultTapirVersion}
import com.softwaremill.adopttapir.starter.api.EffectRequest.{FutureEffect, IOEffect, ZioEffect}
import com.softwaremill.adopttapir.starter.api.RequestValidation.{
  GroupIdShouldFollowJavaPackageConvention,
  ProjectNameShouldBeLowerCaseWritten,
  ProjectNameShouldNotContainWhiteSpaces
}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Akka, Http4s, Netty, ZioHttp}

sealed trait RequestValidation {
  def errMessage: String
}

object RequestValidation {
  case class ProjectNameShouldBeLowerCaseWritten(input: String) extends RequestValidation {
    override val errMessage: String = s"Project name: `$input` should be written with lowercase"
  }

  case class ProjectNameShouldNotContainWhiteSpaces(input: String) extends RequestValidation {
    override val errMessage: String = s"Project name: `$input` should not contain whitespaces"
  }

  case class GroupIdShouldFollowJavaPackageConvention(input: String) extends RequestValidation {
    override val errMessage: String = s"GroupId: `$input` should follow Java package convention"
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

  case class ZIOEffectWillWorkOnlyWithHttp4sAndZioHttp(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation {
    override val errMessage: String = s"$prefixMessage ZIO effect will work only with Http4s and ZioHttp"
  }
}
//TODO: Add semver validator for tapir version
// semver regex
// ^([0-9]+)\.([0-9]+)\.([0-9]+)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+[0-9A-Za-z-]+)?$

sealed trait FormValidator {
  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(r: StarterRequest): Either[IncorrectInput, StarterDetails] =
    (
      validateProjectName(r.projectName),
      validateGroupId(r.groupId),
      validateEffectWithImplementation(r.effect, r.implementation)
    ).mapN { case (projectName, groupId, (effect, serverImplementation)) =>
      effect match {
        case EffectRequest.IOEffect     => IOStarterDetails(projectName, groupId, serverImplementation.toModel(), defaultTapirVersion)
        case EffectRequest.FutureEffect => FutureStarterDetails(projectName, groupId, serverImplementation.toModel(), defaultTapirVersion)
        case EffectRequest.ZioEffect    => ZIOStarterDetails(projectName, groupId, serverImplementation.toModel(), defaultTapirVersion)
      }
    }.leftMap(errors => IncorrectInput(errors.toNonEmptyList.map(_.errMessage).toList.mkString(System.lineSeparator())))
      .toEither

  private def validateProjectName(projectName: String): ValidationResult[String] = {
    val valid = projectName.validNec
    val lowerCase =
      if (projectName != projectName.toLowerCase)
        ProjectNameShouldBeLowerCaseWritten(projectName).invalidNec
      else valid
    val withoutWhitespaces =
      if (projectName.toList.exists(_.isWhitespace))
        ProjectNameShouldNotContainWhiteSpaces(projectName).invalidNec
      else valid

    (lowerCase, withoutWhitespaces).mapN { case (projectName, _) => projectName }
  }

  private def validateGroupId(groupId: String): ValidationResult[String] = {
    if (groupId.matches("^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")) groupId.validNec
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
      case t @ (ZioEffect, Http4s)   => t.validNec
      case t @ (ZioEffect, ZioHttp)  => t.validNec
      case (ZioEffect, _)            => RequestValidation.ZIOEffectWillWorkOnlyWithHttp4sAndZioHttp(effect, serverImplementation).invalidNec
    }
  }
}

object FormValidator extends FormValidator
