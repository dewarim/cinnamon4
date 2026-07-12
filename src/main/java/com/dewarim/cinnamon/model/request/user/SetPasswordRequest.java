package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("setPasswordRequest")
public record SetPasswordRequest(Long userId, String password) implements ApiRequest<SetPasswordRequest> {

    public Optional<SetPasswordRequest> validateRequest() {
        if (password != null && userId != null && userId > 0) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<SetPasswordRequest>> examples() {
        return List.of(new SetPasswordRequest(123L, "my-new-secret-password"));
    }
}
