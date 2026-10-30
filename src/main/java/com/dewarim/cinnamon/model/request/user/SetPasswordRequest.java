package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("setPasswordRequest")
public record SetPasswordRequest(@NotNull @Positive Long userId, @NotNull String password) implements ApiRequest<SetPasswordRequest> {

    public Optional<SetPasswordRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<SetPasswordRequest>> examples() {
        return List.of(new SetPasswordRequest(123L, "my-new-secret-password"));
    }
}
