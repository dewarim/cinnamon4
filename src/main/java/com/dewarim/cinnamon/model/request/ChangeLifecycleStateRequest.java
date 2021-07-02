package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.Optional;

public class ChangeLifecycleStateRequest implements ApiRequest {

    private Long   osdId;
    private String stateName;
    private Long   stateId;

    public ChangeLifecycleStateRequest() {
    }

    public ChangeLifecycleStateRequest(Long osdId, String stateName, Long stateId) {
        this.osdId = osdId;
        this.stateName = stateName;
        this.stateId = stateId;
    }

    private boolean validated() {
        boolean stateNameOkay = stateName != null && stateName.length() > 0;
        boolean stateIdOkay = stateId != null && stateId > 0;
        return osdId != null && osdId > 0 && (stateIdOkay || stateNameOkay);
    }

    public Optional<ChangeLifecycleStateRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }
}
