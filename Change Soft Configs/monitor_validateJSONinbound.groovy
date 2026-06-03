// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Message processData(Message message) {

    def properties = message.getProperties() as Map<String, Object>
    def body       = message.getBody(String)
    def delimiter  = properties.get("csvDelimiter")
    def json
    def error      = ''

    def slurper = new JsonSlurper()
    def parsed
    boolean isJson = false

    if(delimiter == null)
        delimiter = ';'
    
    // is JSON?
    try {
        parsed = slurper.parseText(body)
        if (parsed instanceof Map || parsed instanceof List) {
            isJson = true
        }
    } catch (Exception e) {
        isJson = false
    }

    // if its json, end
    if (!isJson) 
    {
        /////////////////////////////////////////
        //CSV
        /////////////////////////////////////////
        
        // Break CSV \r\n, \n or \r
        List<String> lines = body.split(/\r\n|\n|\r/).toList()
                                .findAll { it != null && it.trim().length() > 0 }

        if (!lines || lines.isEmpty()) {
            message.setBody('[]')
            return message
        }

        // Remove BOM from first line
        lines[0] = lines[0].replaceAll("^\uFEFF", "")

        // Headerr
        List<String> header = lines[0].split(delimiter, -1)*.trim()

        // Index
        int idxIdIflow  = header.indexOf('IdIflow')
        int idxParam    = header.indexOf('Parameter')
        int idxValue    = header.indexOf('Value')

        if (idxIdIflow < 0 || idxParam < 0 || idxValue < 0) {
            error = "CSV missing mandatory columns: IdIflow$delimiter Parameter$delimiter Value. Please check if delimiter is correct."
        }
        // Map: IdIflow -> SoftConfig
        Map<String, List<Map<String, Object>>> byIflow = [:].withDefault { [] }

        // processing line
        lines.tail().each { String line ->
            if (!line || line.trim().isEmpty()) return

            List<String> cols = line.split(delimiter, -1)*.trim()

            int maxIdx = [idxIdIflow, idxParam, idxValue].max()
            if (cols.size() <= maxIdx) {
                // Ignore malformed
                return
            }

            String idIflow = cols[idxIdIflow]
            String param   = cols[idxParam]
            String value   = cols[idxValue]

            // Ignore it without IdIflow or Parameter
            if (!idIflow || !param) {
                return
            }

            byIflow[idIflow] << [
                Parameter: param,
                Value    : value
            ]
        }

        // Map in desired format
        List<Map<String, Object>> result = byIflow.collect { String idIflow, List<Map<String, Object>> softCfgList ->
            [
                IdIflow   : idIflow,
                SoftConfig: softCfgList
            ]
        }

        String jsonStr = JsonOutput.prettyPrint(JsonOutput.toJson(result))

        message.setBody(jsonStr)
        message.setProperty("totalIflows", byIflow.size())
        message.setProperty("jsonInbound", jsonStr)
    }
    else
    {
        /////////////////////////////////////////
        //json
        /////////////////////////////////////////
        
        try 
        {
            json = new JsonSlurper().parseText(body)
        } catch (e) {
            error = "Invalid JSON format."
        }

        // array?
        if (!(json instanceof List)) {
            error = "JSON must be an array at root level."
        }

        json.eachWithIndex { item, idx ->

            // IdIflow
            if (!item?.IdIflow || !(item.IdIflow instanceof String)) {
                error = "Element at index ${idx} missing valid 'IdIflow' (string)."
            }

            // SoftConfig
            if (!(item?.SoftConfig instanceof List)) {
                error = "Element '${item.IdIflow}' missing valid 'SoftConfig' array."
            }

            // config
            item.SoftConfig.eachWithIndex { cfg, cidx ->
                if (!cfg?.Parameter || !(cfg.Parameter instanceof String)) {
                    error = "Invalid 'Parameter' in '${item.IdIflow}' at SoftConfig index ${cidx}."
                }
                if (cfg.Value == null) { // allowed empty, but not null
                    error = "Missing 'Value' in '${item.IdIflow}' at SoftConfig index ${cidx}."
                }
            }
        }
        
        message.setProperty("totalIflows", json.size())
        message.setProperty("jsonInbound", body)
        
    }
    
    def deploy = properties.get("deploy")
        
    if(!deploy)
    {
        message.setProperty("deploy", 'NO')
    }
    else
    {
        if( deploy.toUpperCase() != 'YES')
            message.setProperty("deploy", 'NO')
        else
            message.setProperty("deploy", 'YES')        
    }

    message.setProperty("error", error)

    return message
}
