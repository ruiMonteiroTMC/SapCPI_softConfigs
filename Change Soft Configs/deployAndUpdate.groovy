// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Message processData(Message message) {

    def body       = message.getBody(String)
    def properties = message.getProperties() as Map<String, Object>

    def IdIflowExtracted = properties.get("IdIflowExtracted").toString()
    def finalJson        = properties.get("finalJson").toString()
    def json             = new JsonSlurper().parseText(finalJson)
    def head             = message.getHeaders()
	def httpCode         = head.get("CamelHttpResponseCode")

    def node = json.find { it.IdIflow == IdIflowExtracted }
    if (node != null) {
        node.Deploy = [ Required: "YES", ResponseCode: httpCode, ID: body ]
    }

    def updatedJson = JsonOutput.toJson(json)
    def outJson = JsonOutput.prettyPrint(updatedJson)

    message.setBody(outJson)
    message.setProperty("finalJson", outJson)

    return message
}
