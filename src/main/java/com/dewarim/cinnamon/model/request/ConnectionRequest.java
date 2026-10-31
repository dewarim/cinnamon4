package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

@JsonRootName("connectionRequest")
public record ConnectionRequest(@NotBlank String username, @NotBlank String password, String format) implements ApiRequest<ConnectionRequest> {

    public Optional<ConnectionRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
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
