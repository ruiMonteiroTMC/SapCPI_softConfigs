// Copyright (c) 2026 Rui Monteiro 
// Tenthpin Management Consultants | tenthpin.com

import com.sap.gateway.ip.core.customdev.util.Message
import java.util.HashMap

def Message processData(Message message) {

    def properties = message.getProperties() as Map<String, Object>
    def messageLog = messageLogFactory.getMessageLog(message);
    def body = message.getBody(java.lang.String) 

    def exception = properties.get("CamelExceptionCaught")
    def exceptionMsg = exception?.getMessage()  // Safe call to avoid null pointer

    if (messageLog != null) {
        messageLog.addAttachmentAsString('Soft config report', body, 'text/plain')
    }

    return message
}
