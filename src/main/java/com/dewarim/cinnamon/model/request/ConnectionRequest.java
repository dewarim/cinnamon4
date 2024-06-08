package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "connectionRequest")
public class ConnectionRequest implements ApiRequest<ConnectionRequest> {

    private String username;
    private String password;
    private String format;

    public ConnectionRequest() {
    }

    public ConnectionRequest(String username, String password, String format) {
        this.username = username;
        this.password = password;
        this.format   = format;
    }

    public boolean validated() {
        return username != null && !username.isEmpty() && !username.isBlank()
                && password != null && !password.isEmpty() && !password.isBlank();
    }

    public Optional<ConnectionRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        }
        else {
            return Optional.empty();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return "ConnectionRequest{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", format='" + format + '\'' +
                '}';
    }

    @Override
    public List<ApiRequest<ConnectionRequest>> examples() {
        return List.of(
                new ConnectionRequest("john", "password", "text"),
                new ConnectionRequest("jane", "password", null)
        );
    }
}
