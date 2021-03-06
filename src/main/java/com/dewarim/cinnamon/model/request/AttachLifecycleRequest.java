package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;

public class AttachLifecycleRequest implements ApiRequest {

    private Long osdId;
    private Long lifecycleId;

    /*
     * Optional id of the new state, if null, use default state of the chosen lifecycle.
     */
    private Long lifecycleStateId;

    public AttachLifecycleRequest() {
    }

    public AttachLifecycleRequest(Long osdId, Long lifecycleId, Long lifecycleStateId) {
        this.osdId = osdId;
        this.lifecycleId = lifecycleId;
        this.lifecycleStateId = lifecycleStateId;
    }

    public boolean validated(){
        boolean osdIdOkay = osdId != null && osdId > 0;
        boolean lifecycleIdOkay = lifecycleId != null && lifecycleId > 0;
        boolean stateIdOkay = lifecycleStateId == null || lifecycleStateId > 0;
        return osdIdOkay && lifecycleIdOkay && stateIdOkay;
    }

    public Long getOsdId() {
        return osdId;
    }

    public void setOsdId(Long osdId) {
        this.osdId = osdId;
    }

    public Long getLifecycleId() {
        return lifecycleId;
    }

    public void setLifecycleId(Long lifecycleId) {
        this.lifecycleId = lifecycleId;
    }

    public Long getLifecycleStateId() {
        return lifecycleStateId;
    }

    public void setLifecycleStateId(Long lifecycleStateId) {
        this.lifecycleStateId = lifecycleStateId;
    }
}
