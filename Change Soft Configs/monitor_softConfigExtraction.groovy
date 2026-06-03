// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body        = message.getBody(String)
    def idIflow     = message.getProperty("IdIflowExtracted")?.toString()
    def jsonInbound = message.getProperty("jsonInbound")?.toString()

    def configIndex = (message.getProperty("currentCountSoftConfigs") as Integer)
    def json = new JsonSlurper().parseText(jsonInbound)

    def obj = json.find { it?.IdIflow == idIflow }

    if (configIndex < 0 || configIndex >= obj.SoftConfig.size()) {
        message.setProperty("currentParameter", "")
        message.setProperty("currentParameterValue", "")
    }
    else
    {
        def value     = obj.SoftConfig[configIndex]?.Value?.toString()
        def parameter = obj.SoftConfig[configIndex]?.Parameter?.toString()
        
        message.setProperty("currentParameterValue", value)
        message.setProperty("currentParameter", parameter)
    }

    message.setProperty("currentCountSoftConfigs", configIndex + 1)

    return message
}
