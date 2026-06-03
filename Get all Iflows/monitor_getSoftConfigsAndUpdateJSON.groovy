// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body       = message.getBody(String) ?: '{}'
    def parsed     = new JsonSlurper().parseText(body)
    def results    = parsed?.d?.results ?: []

    def slim = results.collect {
        [
            Parameter   : it?.ParameterKey   ?: "",
            Value       : it?.ParameterValue ?: ""
            //Description : it?.Description    ?: ""
        ]
    }

    def jsonAll   = new JsonSlurper().parseText(message.getProperty("jsonMessage"))
    def packageID = message.getProperty("packageID") ?: ""
    def iflowID   = message.getProperty("iflowID")   ?: ""
    def ignore    = message.getProperty("ignoreParameter")   ?: ""

    def pkg = jsonAll.find { it?.Id == packageID }

    // Ignore list
    def ignoreList = ignore.split(",")
                        .collect { it.trim() }
                        .findAll { it }

    // remove from list
    slim = slim.findAll { r -> !(r?.Parameter in ignoreList) }    

    if (pkg?.Iflows) {
        // Find the iflow
        def flow = pkg.Iflows.find { it?.IdIflow == iflowID }
        if (flow) {
            flow.SoftConfig = slim
        }
    }
                        
    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(jsonAll)))
    message.setProperty("jsonMessage", JsonOutput.toJson(jsonAll) )

    return message
}