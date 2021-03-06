package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.LifecycleState;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LifecycleStateWrapper implements ApiResponse {

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
}
