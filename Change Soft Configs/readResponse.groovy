// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body         = message.getBody(String)
    def jsonPrevious = message.getProperty("jsonPrevious")?.toString()
    def parameterKey = message.getProperty("currentParameter")?.toString()
    def head         = message.getHeaders();
	def httpCode     = head.get("CamelHttpResponseCode");

    def json         = new JsonSlurper().parseText(jsonPrevious)

    def results      = json?.d?.results
    def node         = results.find { it?.ParameterKey == parameterKey }
    def value        = node?.ParameterValue?.toString()

    message.setProperty("beforeParameterValue", value)
    message.setProperty("responseMessage", body)
    message.setProperty("responseCode", httpCode)

    return message
}
