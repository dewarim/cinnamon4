package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateLifecycleStateRequest")
public record UpdateLifecycleStateRequest(
        @JacksonXmlElementWrapper(localName = "lifecycleStates")
        @JacksonXmlProperty(localName = "lifecycleState")
        @NotEmpty List<@Valid LifecycleState> lifecycleStates) implements UpdateRequest<LifecycleState>, ApiRequest<UpdateLifecycleStateRequest> {

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
