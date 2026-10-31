package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("changeLifecycleStateRequest")
public record ChangeLifecycleStateRequest(@NotNull @Positive Long osdId, @NotNull @Positive Long stateId) implements ApiRequest<ChangeLifecycleStateRequest> {

    public Optional<ChangeLifecycleStateRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ChangeLifecycleStateRequest>> examples() {
        return List.of(new ChangeLifecycleStateRequest(5L, 32L));
    }
}
