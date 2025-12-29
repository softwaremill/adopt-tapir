package com.softwaremill.adopttapir.starter.api

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*
import com.softwaremill.adopttapir.Fail.*
import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.starter.api.StackRequest.ZIOStack
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

  abstract class StackValidation:
    val stack: StackRequest
    val implementation: ServerImplementationRequest
    protected val prefixMessage: String = s"Picked $stack with $implementation -"

  case class StackWithIllegalServerImplementation(stack: StackRequest, implementation: ServerImplementationRequest)
      extends StackValidation
      with RequestValidation:
    override val errMessage: String =
      s"$prefixMessage ${stack.toModel.name} stack will work only with: ${stack.legalServerImplementations.map(_.name).mkString(", ")}"

  case object ZIOJsonWillWorkOnlyWithZIOStack extends RequestValidation:
    override val errMessage: String = s"ZIOJson will work only with ZIO stack"


end RequestValidation

sealed trait FormValidator:
  type ValidationResult[A] = ValidatedNec[RequestValidation, A]

  def validate(r: StarterRequest): Either[IncorrectInput, StarterDetails] =
    (
      validateProjectName(r.projectName),
      validateGroupId(r.groupId),
      validateStackWithImplementation(r.stack, r.implementation, r.scalaVersion),
      validateMetrics(r.stack, r.implementation, r.addMetrics),
      validateJson(r.stack, r.json)
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

  private def validateStackWithImplementation(
      stackRequest: StackRequest,
      serverImplementation: ServerImplementationRequest,
      scalaVersionRequest: ScalaVersionRequest
  ): ValidatedNec[RequestValidation, (StackRequest, ServerImplementationRequest)] =
    Validated.condNec(
      stackRequest.legalServerImplementations.contains(
        serverImplementation.toModel
      ) && !(stackRequest == StackRequest.OxStack && scalaVersionRequest == ScalaVersionRequest.Scala2),
      (stackRequest, serverImplementation),
      RequestValidation.StackWithIllegalServerImplementation(stackRequest, serverImplementation)
    )

  private def validateMetrics(
      stackRequest: StackRequest,
      serverImplementation: ServerImplementationRequest,
      addMetrics: Boolean
  ): ValidatedNec[RequestValidation, Boolean] =
    (stackRequest, serverImplementation, addMetrics) match {
      case t @ (_, Http4s | ZIOHttp | Netty | VertX | Pekko, _) => t._3.validNec
    }

  private def validateJson(
      stackRequest: StackRequest,
      json: JsonImplementationRequest
  ): ValidatedNec[RequestValidation, JsonImplementationRequest] =
    validateStackWithJson(stackRequest, json)

  private def validateStackWithJson(
      stackRequest: StackRequest,
      json: JsonImplementationRequest
  ): ValidatedNec[RequestValidation, JsonImplementationRequest] =
    (stackRequest, json) match {
      case t @ (ZIOStack, ZIOJson) => t._2.validNec
      case (_, ZIOJson)            => RequestValidation.ZIOJsonWillWorkOnlyWithZIOStack.invalidNec
      case t                       => t._2.validNec
    }

end FormValidator

object FormValidator extends FormValidator
