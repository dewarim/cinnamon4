package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@JacksonXmlRootElement(localName = "connection")
public class CinnamonConnection implements Wrapper<CinnamonConnection>, ApiResponse {

    private String ticket;

    public CinnamonConnection() {
    }

    public CinnamonConnection(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }

    @Override
    public List<CinnamonConnection> list() {
        return Collections.singletonList(this);
    }

    @Override
    public Wrapper<CinnamonConnection> setList(List<CinnamonConnection> cinnamonConnections) {
        if (cinnamonConnections == null || cinnamonConnections.size() > 1) {
            throw new IllegalStateException("Only lists containing a single CinnamonConnection are allowed.");
        }
        this.ticket = cinnamonConnections.get(0).ticket;
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new CinnamonConnection(UUID.randomUUID().toString()));
    }
}
