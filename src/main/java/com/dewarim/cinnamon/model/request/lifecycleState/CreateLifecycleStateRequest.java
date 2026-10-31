package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createLifecycleStateRequest")
public record CreateLifecycleStateRequest(
        @JacksonXmlElementWrapper(localName = "lifecycleStates")
        @JacksonXmlProperty(localName = "lifecycleState")
        @NotEmpty List<@Valid LifecycleState> lifecycleStates) implements CreateRequest<LifecycleState>, ApiRequest<LifecycleState> {

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
