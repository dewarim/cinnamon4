package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "changeLifecycleStateRequest")
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
