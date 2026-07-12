package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("connectionRequest")
public record ConnectionRequest(String username, String password, String format) implements ApiRequest<ConnectionRequest> {

    public boolean validated() {
        return username != null && !username.isEmpty() && !username.isBlank()
                && password != null && !password.isEmpty() && !password.isBlank();
    }

    public Optional<ConnectionRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        // never expose the password in logs
        return "ConnectionRequest{" +
                "username='" + username + '\'' +
                ", password='" + (password == null ? null : "***censored***") + '\'' +
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
