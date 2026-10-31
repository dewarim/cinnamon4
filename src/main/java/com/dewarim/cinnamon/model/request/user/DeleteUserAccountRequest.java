package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("deleteUserAccountRequest")
public record DeleteUserAccountRequest(@NotNull @Positive Long userId,
                                       @NotNull @Positive Long assetReceiverId) implements ApiRequest<DeleteUserAccountRequest> {

    public Optional<DeleteUserAccountRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<DeleteUserAccountRequest>> examples() {
        return List.of(new DeleteUserAccountRequest(4L, 5L));
    }
}
