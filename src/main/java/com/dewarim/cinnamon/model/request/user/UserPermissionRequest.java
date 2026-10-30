package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("userPermissionRequest")
public record UserPermissionRequest(@NotNull @Positive Long userId, @NotNull @Positive Long aclId) implements ApiRequest<UserPermissionRequest> {

    public Optional<UserPermissionRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<UserPermissionRequest>> examples() {
        return List.of(new UserPermissionRequest(6L, 7L));
    }
}
