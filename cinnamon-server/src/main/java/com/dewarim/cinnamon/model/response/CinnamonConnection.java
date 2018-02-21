package com.dewarim.cinnamon.model.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "connection")
public class CinnamonConnection {

    private String ticket;

    public CinnamonConnection() {
    }

    public CinnamonConnection(String ticket) {
        this.ticket = ticket;
    }

    public String getTicket() {
        return ticket;
    }
    
    
}
