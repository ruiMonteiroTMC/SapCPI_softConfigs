// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def iflowJSON     = message.getProperty("jsonMessage")
    def packageID     = message.getProperty("packageID")
    def targetIflowIx = message.getProperty("currentCountIflow").toInteger()

    def json            = new JsonSlurper().parseText(iflowJSON)
    def selectedPackage = json.find { it.Id == packageID }
    resultValue         = selectedPackage.Iflows[targetIflowIx]?.IdIflow
        
    if(resultValue)
    {       
        message.setProperty("iflowID", resultValue)
        message.setProperty("currentCountIflow", targetIflowIx + 1)
    }

    return message
}