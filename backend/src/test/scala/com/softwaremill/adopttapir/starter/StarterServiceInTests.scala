package com.softwaremill.adopttapir.starter

import com.softwaremill.adopttapir.template.{GeneratedFile, ProjectTemplateInTests}

class StarterServiceInTests(config: StarterConfig, template: ProjectTemplateInTests) extends StarterService(config, template) {
  override protected def generateFiles(starterDetails: StarterDetails): List[GeneratedFile] =
    super.generateFiles(starterDetails)
      .appendedAll(if (starterDetails.addMetrics) List(template.getFrameworkEndpointsSpec(starterDetails)) else List())
}
