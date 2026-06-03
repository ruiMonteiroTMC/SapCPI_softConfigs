// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body = message.getBody(String) ?: '{}'
    def json = new JsonSlurper().parseText(body)

    def results = (json?.d?.results ?: []) as List
    def count = results.size()

    def slim = results.collect { r ->
        [
            IdIflow        : r?.Id ?: "",
            NameIflow      : r?.Name ?: "",
            ShortTextIflow : r?.Description ?: ""
        ]
    }
    
    def jsonMessageAll   = (message.getProperty("jsonMessage") as String) ?: "[]"
    def jsonAll          = new JsonSlurper().parseText(jsonMessageAll)
    def targetPackage    = (message.getProperty("packageID") as String)

    found = false
    jsonAll.each { item ->
        if ((item instanceof Map) && String.valueOf(item.Id) == targetPackage) {
            item.Iflows = slim 
            found = true
        }
    }

    def outJson = JsonOutput.prettyPrint(JsonOutput.toJson(jsonAll))
    message.setBody(outJson)

    message.setProperty("jsonMessage", JsonOutput.toJson(jsonAll))
    message.setProperty("totalIflows", count)

    return message
}
