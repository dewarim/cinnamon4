package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("createLifecycleStateRequest")
public record CreateLifecycleStateRequest(
        @JacksonXmlElementWrapper(localName = "lifecycleStates")
        @JacksonXmlProperty(localName = "lifecycleState")
        List<LifecycleState> lifecycleStates) implements CreateRequest<LifecycleState>, ApiRequest<LifecycleState> {

    public CreateLifecycleStateRequest {
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
                        Objects.isNull(lifecycleState.getStateClass()) ||
                        Objects.isNull(lifecycleState.getName()) ||
                        (lifecycleState.getStateClass() == null || lifecycleState.getStateClass().isBlank()) ||
                        lifecycleState.getName().isBlank() ||
                        (lifecycleState.getConfig() == null || lifecycleState.getConfig().isBlank())
        );
    }

    @Override
    public Wrapper<LifecycleState> fetchResponseWrapper() {
        return new LifecycleStateWrapper();
    }

    @Override
    public List<ApiRequest<LifecycleState>> examples() {
        return List.of(new CreateLifecycleStateRequest(List.of(
                new LifecycleState("review-state", "<config/>", NopState.class.getName(), 1L, null),
                new LifecycleState("authoring-state",
                        "<config><properties><property><name>aclName</name><value>_default_acl</value></property></properties><nextStates><name>review-state</name></nextStates></config>",
                        ChangeAclState.class.getName(), 2L, 3L)
        )));
    }
}
