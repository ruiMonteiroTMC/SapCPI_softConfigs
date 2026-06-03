// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

Message processData(Message message) {

    def body          = message.getBody(String) ?: '{}'
    def ignore        = message.getProperty("ignorePackages")?.toString() ?: ""
    def returnPackage = message.getProperty("returnPackage")?.toString() ?: ""
    def errorMsg      = ''
    
    def json = new JsonSlurper().parseText(body)
    def results = (json?.d?.results ?: []) as List


    if(returnPackage != "")
    {
         // returnPackage list
         def returnList = returnPackage.split(",")
                           .collect { it.trim() }
                           .findAll { it }


       // Filter only if package requested (if defined)
        results = results.findAll { r -> r?.Id in returnList }
        
    }
    else
    {
         // ignore list
        def ignoreList = ignore.split(",")
                           .collect { it.trim() }
                           .findAll { it }
                           
                           
        // remove from list
        results = results.findAll { r -> !(r?.Id in ignoreList) }                   
    }
   

    def count = results.size()

    def slim = results.collect { r ->
        [
            Id        : r?.Id ?: "",
            Name      : r?.Name ?: "",
            ShortText : r?.ShortText ?: ""
        ]
    }

    def outJson = JsonOutput.prettyPrint(JsonOutput.toJson(slim))
    message.setBody(outJson)

    message.setProperty("jsonMessage", JsonOutput.toJson(slim))
    message.setProperty("totalPackages", count)

    return message
}
