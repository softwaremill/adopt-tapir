package com.softwaremill.adopttapir.starter.api

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*
import com.softwaremill.adopttapir.Fail.*
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.api.EffectRequest.ZIOEffect
import com.softwaremill.adopttapir.starter.api.JsonImplementationRequest.ZIOJson
import com.softwaremill.adopttapir.starter.api.RequestValidation.{GroupIdShouldFollowJavaPackageConvention, ProjectNameShouldMatchRegex}
import com.softwaremill.adopttapir.starter.api.ServerImplementationRequest.{Http4s, Netty, Pekko, VertX, ZIOHttp}

sealed trait RequestValidation:
  def errMessage: String

object RequestValidation:

  case class ProjectNameShouldMatchRegex(input: String, regex: String) extends RequestValidation:
    override val errMessage: String = s"Project name: `$input` should match regex: `$regex`"

  case class GroupIdShouldFollowJavaPackageConvention(input: String) extends RequestValidation:
    override val errMessage: String = s"GroupId: `$input` should follow Java package convention and be smaller than 256 characters"

  abstract class EffectValidation:
    val effect: EffectRequest
    val implementation: ServerImplementationRequest
    protected val prefixMessage: String = s"Picked $effect with $implementation -"

  case class EffectWithIllegalServerImplementation(effect: EffectRequest, implementation: ServerImplementationRequest)
      extends EffectValidation
      with RequestValidation:
    override val errMessage: String =
      s"$prefixMessage ${effect.toModel.name} effect will work only with: ${effect.legalServerImplementations.map(_.name).mkString(", ")}"

  case object ZIOJsonWillWorkOnlyWithZIOEffect extends RequestValidation:
    override val errMessage: String = s"ZIOJson will work only with ZIO effect"

end RequestValidation

sealed trait FormValidator:
  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(r: StarterRequest): Either[IncorrectInput, StarterDetails] =
    (
      validateProjectName(r.projectName),
      validateGroupId(r.groupId),
      validateEffectWithImplementation(r.effect, r.implementation),
      validateMetrics(r.effect, r.implementation, r.addMetrics),
      validateEffectWithJson(r.effect, r.json)
    ).mapN { case (projectName, groupId, (effect, serverImplementation), addMetrics, json) =>
      StarterDetails(
        projectName,
        groupId,
        effect.toModel,
        serverImplementation.toModel,
        r.addDocumentation,
        addMetrics,
        json.toModel,
        r.scalaVersion.toModel,
        r.builder.toModel
      )
    }.leftMap(errors => IncorrectInput(errors.toNonEmptyList.map(_.errMessage).toList.mkString(System.lineSeparator())))
      .toEither

  private def validateProjectName(projectName: String): ValidationResult[String] =
    val projectNameRgx = "^[a-z0-9_]$|^[a-z0-9_]+[a-z0-9_-]*[a-z0-9_]+$"

    if projectName.matches(projectNameRgx) then projectName.validNec
    else ProjectNameShouldMatchRegex(projectName, projectNameRgx).invalidNec

  private def validateGroupId(groupId: String): ValidationResult[String] =
    if groupId.matches("(?:^[a-z][a-z0-9_]*|[a-z][a-z0-9_]*\\.[a-z0-9_]+)+$") && groupId.length <= 256 then groupId.validNec
    else GroupIdShouldFollowJavaPackageConvention(groupId).invalidNec

  private def validateEffectWithImplementation(
      effect: EffectRequest,
      serverImplementation: ServerImplementationRequest
  ): ValidatedNec[RequestValidation, (EffectRequest, ServerImplementationRequest)] =
    Validated.condNec(
      effect.legalServerImplementations.contains(serverImplementation.toModel),
      (effect, serverImplementation),
      RequestValidation.EffectWithIllegalServerImplementation(effect, serverImplementation)
    )

  private def validateMetrics(
      effect: EffectRequest,
      serverImplementation: ServerImplementationRequest,
      addMetrics: Boolean
  ): ValidatedNec[RequestValidation, Boolean] =
    (effect, serverImplementation, addMetrics) match {
      case t @ (_, Http4s | ZIOHttp | Netty | VertX | Pekko, _) => t._3.validNec
    }

  private def validateEffectWithJson(
      effectRequest: EffectRequest,
      json: JsonImplementationRequest
  ): ValidatedNec[RequestValidation, JsonImplementationRequest] =
    (effectRequest, json) match {
      case t @ (ZIOEffect, ZIOJson) => t._2.validNec
      case (_, ZIOJson)             => RequestValidation.ZIOJsonWillWorkOnlyWithZIOEffect.invalidNec
      case t                        => t._2.validNec
    }

end FormValidator

object FormValidator extends FormValidator
