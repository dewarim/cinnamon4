package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.lifecycle.ChangeAclState;
import com.dewarim.cinnamon.model.LifecycleState;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LifecycleStateWrapper extends BaseResponse implements Wrapper<LifecycleState>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "lifecycleStates")
    @JacksonXmlProperty(localName = "lifecycleState")
    List<LifecycleState> lifecycleStates = new ArrayList<>();

    public LifecycleStateWrapper() {
    }

    public LifecycleStateWrapper(List<LifecycleState> lifecycleStates) {
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
    public Wrapper<LifecycleState> setList(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
        return this;
    }

    @Override
    public List<Object> examples() {
        LifecycleStateWrapper lifecycleStateWrapper = new LifecycleStateWrapper();
        LifecycleState        lifecycleState        = new LifecycleState("published", "<config/>", ChangeAclState.class.getName(), 6L, 77L);
        lifecycleState.setId(79L);
        lifecycleStateWrapper.getLifecycleStates().add(lifecycleState);
        return List.of(new LifecycleStateWrapper(List.of(lifecycleState)));
    }
}
