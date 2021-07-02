package com.dewarim.cinnamon.model.response;


import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Lifecycle;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class LifecycleWrapper implements Wrapper<Lifecycle>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "lifecycles")
    @JacksonXmlProperty(localName = "lifecycle")
    List<Lifecycle> lifecycles = new ArrayList<>();

    public List<Lifecycle> getLifecycles() {
        return lifecycles;
    }

    public void setLifecycles(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    @Override
    public List<Lifecycle> list() {
        return getLifecycles();
    }

    @Override
    public Wrapper<Lifecycle> setList(List<Lifecycle> lifecycles) {
        setLifecycles(lifecycles);
        return this;
    }
}
