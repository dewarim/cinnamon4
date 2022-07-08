package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "changeLifecycleStateRequest")
public class ChangeLifecycleStateRequest implements ApiRequest {

    private Long   osdId;
    private Long   stateId;

    public ChangeLifecycleStateRequest() {
    }

    public ChangeLifecycleStateRequest(Long osdId, Long stateId) {
        this.osdId = osdId;
        this.stateId = stateId;
    }

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

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }
}
