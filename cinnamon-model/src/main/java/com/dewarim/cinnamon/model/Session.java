package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Session implements Identifiable {
    
    private Long id;
    private String ticket;
    private Date expires;
    private Long userId;

    public Session() {
    }

    public Session(Long userId, long sessionLengthInMillis) {
        ticket = UUID.randomUUID().toString();
        Date now = new Date();
        expires = new Date(now.getTime()+sessionLengthInMillis);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        return Objects.equals(id, session.id) && Objects.equals(ticket, session.ticket) && Objects.equals(expires, session.expires) && Objects.equals(userId, session.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticket, expires, userId);
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", ticket='" + ticket + '\'' +
                ", expires=" + expires +
                ", userId=" + userId +
                '}';
    }
}
