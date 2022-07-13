package com.softwaremill.adopttapir.template

import com.softwaremill.adopttapir.starter.ServerEffect.ZIOEffect
import com.softwaremill.adopttapir.starter.{StarterConfig, StarterDetails}
import com.softwaremill.adopttapir.template.ProjectTemplate.toSortedList
import com.softwaremill.adopttapir.template.scala.{EndpointsSpecView, FrameworkEndpointsSpecView}

class ProjectTemplateInTests(config: StarterConfig) extends ProjectTemplate(config) {
  def getFrameworkEndpointsSpec(starterDetails: StarterDetails): GeneratedFile = {
    val groupId = starterDetails.groupId

    val metricsServerStub = FrameworkEndpointsSpecView.getMetricsServerStub(starterDetails)

    val fileContent =
      if (starterDetails.serverEffect == ZIOEffect) {
        txt
          .FrameworkEndpointsSpecZIO(
            starterDetails,
            toSortedList(metricsServerStub.imports),
            metricsServerStub.body
          )
      } else {
        val unwrapper = EndpointsSpecView.Unwrapper.prepareUnwrapper(starterDetails.serverEffect)
        txt
          .FrameworkEndpointsSpec(
            starterDetails,
            toSortedList(metricsServerStub.imports ++ unwrapper.imports),
            metricsServerStub.body,
            unwrapper.body
          )
      }


    GeneratedFile(
      pathUnderPackage("src/test/scala", groupId, "FrameworkEndpointsSpec.scala"),
      fileContent.toString
    )
  }
}
