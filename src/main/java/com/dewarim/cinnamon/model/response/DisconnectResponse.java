package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collections;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class DisconnectResponse implements Wrapper<DisconnectResponse>, ApiResponse {

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

    @Override
    public List<DisconnectResponse> list() {
        return Collections.singletonList(this);
    }

    @Override
    public Wrapper<DisconnectResponse> setList(List<DisconnectResponse> disconnectResponses) {
        if (disconnectResponses == null || disconnectResponses.size() != 1) {
            throw new IllegalStateException("Only lists containing a single DisconnectResponse are allowed.");
        }
        this.disconnectSuccessful = disconnectResponses.get(0).disconnectSuccessful;
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new DisconnectResponse(true));
    }
}
