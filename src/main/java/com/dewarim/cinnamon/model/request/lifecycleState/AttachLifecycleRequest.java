package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("attachLifecycleRequest")
public record AttachLifecycleRequest(Long osdId, Long lifecycleId, Long lifecycleStateId, boolean forceChange) implements ApiRequest<AttachLifecycleRequest> {

    public boolean validated() {
        boolean osdIdOkay       = osdId != null && osdId > 0;
        boolean lifecycleIdOkay = lifecycleId != null && lifecycleId > 0;
        boolean stateIdOkay     = lifecycleStateId == null || lifecycleStateId > 0;
        return osdIdOkay && lifecycleIdOkay && stateIdOkay;
    }

    @Override
    public List<ApiRequest<AttachLifecycleRequest>> examples() {
        return List.of(new AttachLifecycleRequest(4L, 6L, 2L, true));
    }
}
