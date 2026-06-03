// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

Message processData(Message message) {

    def properties = message.getProperties() as Map<String, Object>
    def body       = message.getBody(String)
    def json       = new JsonSlurper().parseText(body)
    def delimiter  = properties.get("csvDelimiter")
    def separator  = ';' //default

    if(delimiter)
        separator = delimiter

    if (!(json instanceof List)) {
        json = [json]
    }

    def nl = '\n'

    // HEADER
    StringBuilder csv = new StringBuilder()
    csv.append(["PackageID","PackageName","IdIflow","NameIflow","Parameter","Value"].join(separator))
       .append(nl)

    // Clear escapes
    def esc = { v ->
        if (v == null) return ""
        def s = v.toString()
        def needsQuotes = s.contains(separator) || s.contains('"') || s.contains('\n') || s.contains('\r')
        s = s.replace('"', '""')
        needsQuotes ? "\"${s}\"" : s
    }

    // packages
    json.each { pkg ->
        def pkgId   = pkg.Id
        def pkgName = pkg.Name
        def iflows  = pkg.Iflows ?: []

        // iFlows
        iflows.each { iflow ->
            def iflowId   = iflow.IdIflow
            def iflowName = iflow.NameIflow
            def softCfgs  = iflow.SoftConfig ?: []

            // SoftConfig
            softCfgs.each { cfg ->
                def param = cfg.Parameter
                def value = cfg.Value

                csv.append([
                    esc(pkgId),
                    esc(pkgName),
                    esc(iflowId),
                    esc(iflowName),
                    esc(param),
                    esc(value)
                ].join(separator)).append(nl)
            }
        }
    }

    message.setBody(csv.toString())
    return message
}
