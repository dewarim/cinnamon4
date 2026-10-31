package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("attachLifecycleRequest")
public record AttachLifecycleRequest(@NotNull @Positive Long osdId,
                                     @NotNull @Positive Long lifecycleId,
                                     @Positive Long lifecycleStateId,
                                     boolean forceChange) implements ApiRequest<AttachLifecycleRequest> {

    public Optional<AttachLifecycleRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<AttachLifecycleRequest>> examples() {
        return List.of(new AttachLifecycleRequest(4L, 6L, 2L, true));
    }
}
