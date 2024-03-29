package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.lifecycle.NopState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "createLifecycleStateRequest")
public class CreateLifecycleStateRequest implements CreateRequest<LifecycleState>, ApiRequest<LifecycleState> {

    @JacksonXmlElementWrapper(localName = "lifecycleStates")
    @JacksonXmlProperty(localName = "lifecycleState")
    private List<LifecycleState> lifecycleStates = new ArrayList<>();

    public CreateLifecycleStateRequest() {
    }

    public CreateLifecycleStateRequest(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }

    public List<LifecycleState> getLifecycleStates() {
        return lifecycleStates;
    }

    public void setLifecycleStates(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
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
                                (lifecycleState.getStateClass() == null || lifecycleState.getStateClass().isBlank()  ) ||
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
