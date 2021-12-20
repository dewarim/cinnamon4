package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "updateLifecycleStateRequest")
public class UpdateLifecycleStateRequest implements UpdateRequest<LifecycleState>, ApiRequest {

    private List<LifecycleState> lifecycleStates = new ArrayList<>();

    @Override
    public List<LifecycleState> list() {
        return lifecycleStates;
    }

    public UpdateLifecycleStateRequest() {
    }

    public UpdateLifecycleStateRequest(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }

    public List<LifecycleState> getLifecycleStates() {
        return lifecycleStates;
    }

    @Override
    public boolean validated() {
        if(lifecycleStates == null || lifecycleStates.isEmpty()){
            return false;
        }
        return lifecycleStates.stream().noneMatch(lifecycleState ->
                Objects.isNull(lifecycleState) ||
                        Objects.isNull(lifecycleState.getId()) ||
                        Objects.isNull(lifecycleState.getName()) ||
                        lifecycleState.getId() < 1 ||
                        lifecycleState.getName().isBlank() ||
                        (lifecycleState.getConfig() == null || lifecycleState.getConfig().isBlank()));
    }

    @Override
    public Wrapper<LifecycleState> fetchResponseWrapper() {
        return new LifecycleStateWrapper();
    }
}
