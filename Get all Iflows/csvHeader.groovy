// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message

Message processData(Message message) {
    
    def properties = message.getProperties() as Map<String, Object>
    def body       = message.getBody(String)
    def csv        = properties.get("isCSV")
    
    if(!csv)
    {
        message.setProperty("isCSV", 'NO')
    }
    else
    {
        if( csv.toUpperCase() != 'YES')
            message.setProperty("isCSV", 'NO')
        else
            message.setProperty("isCSV", 'YES')        
    }

    return message
}
