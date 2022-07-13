package com.softwaremill.adopttapir.template.scala

import com.softwaremill.adopttapir.starter.StarterDetails
import com.softwaremill.adopttapir.template.scala.EndpointsSpecView.Stub
import com.softwaremill.adopttapir.template.scala.EndpointsView.Constants.metricsEndpoint

object FrameworkEndpointsSpecView {
  def getMetricsServerStub(starterDetails: StarterDetails): Code = {
    if (starterDetails.addMetrics) Stub.prepareBackendStub(metricsEndpoint, starterDetails.serverEffect) else Code.empty
  }
}
