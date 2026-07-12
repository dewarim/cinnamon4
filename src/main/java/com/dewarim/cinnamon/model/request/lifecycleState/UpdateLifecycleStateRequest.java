package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("updateLifecycleStateRequest")
public record UpdateLifecycleStateRequest(
        @JacksonXmlElementWrapper(localName = "lifecycleStates")
        @JacksonXmlProperty(localName = "lifecycleState")
        List<LifecycleState> lifecycleStates) implements UpdateRequest<LifecycleState>, ApiRequest<UpdateLifecycleStateRequest> {

    public UpdateLifecycleStateRequest {
        if (lifecycleStates == null) {
            lifecycleStates = new ArrayList<>();
        }
    }

    @Override
    public List<LifecycleState> list() {
        return lifecycleStates;
    }

    @Override
    public boolean validated() {
        if (lifecycleStates == null || lifecycleStates.isEmpty()) {
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

    @Override
    public List<ApiRequest<UpdateLifecycleStateRequest>> examples() {
        var lifecycleState = new LifecycleState("review-state-update", "<config/>", ChangeAclState.class.getName(), 1L, null);
        lifecycleState.setId(232L);
        return List.of(new UpdateLifecycleStateRequest(List.of(lifecycleState)));
    }
}
