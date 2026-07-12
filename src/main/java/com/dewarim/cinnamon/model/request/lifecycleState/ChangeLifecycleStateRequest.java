package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("changeLifecycleStateRequest")
public record ChangeLifecycleStateRequest(Long osdId, Long stateId) implements ApiRequest<ChangeLifecycleStateRequest> {

    private boolean validated() {
        boolean stateIdOkay = stateId != null && stateId > 0;
        return osdId != null && osdId > 0 && stateIdOkay;
    }

    public Optional<ChangeLifecycleStateRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<ChangeLifecycleStateRequest>> examples() {
        return List.of(new ChangeLifecycleStateRequest(5L, 32L));
    }
}
