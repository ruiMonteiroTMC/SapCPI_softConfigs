// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body  = message.getProperty("jsonInbound") as String
    def idx  = message.getProperty("currentCountIflow") as Integer
    def json = new JsonSlurper().parseText(body)

    if (idx < 0 || idx >= json.size()) {
        idIflow = ''
    }

    def idIflow = json[idx]?.IdIflow ?: ""

    message.setProperty("IdIflowExtracted", idIflow)
    message.setProperty("currentCountIflow", idx + 1 )
    message.setProperty("totalSoftConfigs", json[idx]?.SoftConfig.size() )
    message.setProperty("currentCountSoftConfigs", 0 )

    return message
}
