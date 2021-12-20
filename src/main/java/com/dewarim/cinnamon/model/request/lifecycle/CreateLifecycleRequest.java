package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "createLifecycleRequest")
public class CreateLifecycleRequest implements CreateRequest<Lifecycle>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "lifecycles")
    @JacksonXmlProperty(localName = "lifecycle")
    private List<Lifecycle> lifecycles = new ArrayList<>();

    public CreateLifecycleRequest() {
    }

    public CreateLifecycleRequest(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    public List<Lifecycle> getLifecycles() {
        return lifecycles;
    }

    public void setLifecycles(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    @Override
    public List<Lifecycle> list() {
        return lifecycles;
    }

    @Override
    public boolean validated() {
        if (lifecycles == null || lifecycles.isEmpty()) {
            return false;
        }
        return lifecycles.stream().noneMatch(lifecycle ->
                Objects.isNull(lifecycle) ||
                        Objects.isNull(lifecycle.getName()) ||
                        (!Objects.isNull(lifecycle.getDefaultStateId()) && lifecycle.getDefaultStateId() < 1L) ||
                        lifecycle.getName().isBlank()
        );
    }

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }
}
