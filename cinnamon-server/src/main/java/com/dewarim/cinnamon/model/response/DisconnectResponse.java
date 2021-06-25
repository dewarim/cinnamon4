package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "cinnamon")
public class DisconnectResponse {
    
    boolean disconnectSuccessful;

    public DisconnectResponse() {
    }

    public DisconnectResponse(boolean disconnectSuccessful) {
        this.disconnectSuccessful = disconnectSuccessful;
    }

    public boolean isDisconnectSuccessful() {
        return disconnectSuccessful;
    }

    public void setDisconnectSuccessful(boolean disconnectSuccessful) {
        this.disconnectSuccessful = disconnectSuccessful;
    }
}
