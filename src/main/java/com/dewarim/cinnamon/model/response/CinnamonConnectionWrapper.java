package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class CinnamonConnectionWrapper extends BaseResponse implements Wrapper<CinnamonConnection>, ApiResponse {

    @JsonProperty("cinnamonConnection")
    private CinnamonConnection cinnamonConnection;

    public CinnamonConnectionWrapper() {
    }

    public CinnamonConnectionWrapper(CinnamonConnection cinnamonConnection) {
        this.cinnamonConnection = cinnamonConnection;
    }

    @Override
    public List<CinnamonConnection> list() {
        return List.of(cinnamonConnection);
    }

    @Override
    public Wrapper<CinnamonConnection> setList(List<CinnamonConnection> cinnamonConnections) {
        this.cinnamonConnection = cinnamonConnections.get(0);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new CinnamonConnectionWrapper(new CinnamonConnection("64772ea0-0184-4f94-96d4-6348d88e9e82")));
    }

    @Override
    public String toString() {
        return "ConnectionResponse{" +
                "cinnamonConnection=" + cinnamonConnection +
                '}';
    }
}
