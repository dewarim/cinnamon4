package com.dewarim.cinnamon.model;

import java.util.Date;
import java.util.UUID;

public class Session {
    
    private Long id;
    private String ticket;
    private Date expires;
    private Long userId;

    public Session() {
    }

    public Session(Long userId) {
        ticket = UUID.randomUUID().toString();
        Date now = new Date();
        // TODO: make expiration configurable
        expires = new Date(now.getTime()+3600_000);
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
