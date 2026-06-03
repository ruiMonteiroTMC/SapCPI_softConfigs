// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body           = message.getProperty("finalJson")?.toString() ?: "[]"
    def idIflow        = message.getProperty("IdIflowExtracted")?.toString()
    def parameter      = message.getProperty("currentParameter")?.toString()
    def valueBefore    = message.getProperty("beforeParameterValue")?.toString() ?: ""
    def valueUpdated   = message.getProperty("currentParameterValue")?.toString() ?: ""
    def responseCode   = message.getProperty("responseCode")?.toString() ?: ""
    def responseMsg    = message.getProperty("responseMessage")?.toString() ?: ""
    def deploy         = message.getProperty("deploy")?.toString() ?: ""

    def json
    try {
        json = new JsonSlurper().parseText(body)
    } catch (e) {
        json = []
    }
    if (!(json instanceof List)) {
        json = []
    }

    def node = json.find { it?.IdIflow == idIflow }
    if (!node) {
        node = [ IdIflow: idIflow, Deploy: [Required: deploy], SoftConfig: [] ]
        json << node
    }
    if (!(node.SoftConfig instanceof List)) {
        node.SoftConfig = []
    }

    def newCfg = [
        Parameter      : parameter,
        ValueBefore    : valueBefore,
        ValueUpdated   : valueUpdated,
        ResponseCode   : responseCode,
        ResponseMessage: responseMsg
    ]

    def existingIdx = node.SoftConfig.findIndexOf { it?.Parameter == parameter }
    if (existingIdx >= 0) {
        node.SoftConfig[existingIdx] = newCfg
    } else {
        node.SoftConfig << newCfg
    }

    def outJson = JsonOutput.prettyPrint(JsonOutput.toJson(json))
    message.setBody(outJson)

    message.setProperty("finalJson", outJson)
   
    return message
}
