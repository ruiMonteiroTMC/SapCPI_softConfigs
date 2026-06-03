// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def count  = message.getProperty("currentCountPackage").toInteger()
    def body   = message.getProperty("jsonMessage")

    def json   = new JsonSlurper().parseText(body)

    def idValue = json[count]?.Id

    message.setProperty("packageID", idValue)
    message.setProperty("currentCountPackage", count + 1)
    message.setProperty("currentCountIflow", 0)

    return message
}