package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "updateLifecycleRequest")
public class UpdateLifecycleRequest implements UpdateRequest<Lifecycle>, ApiRequest {

    private List<Lifecycle> lifecycles = new ArrayList<>();

    @Override
    public List<Lifecycle> list() {
        return lifecycles;
    }

    public UpdateLifecycleRequest() {
    }

    public UpdateLifecycleRequest(List<Lifecycle> lifecycles) {
        this.lifecycles = lifecycles;
    }

    public List<Lifecycle> getLifecycles() {
        return lifecycles;
    }

    @Override
    public boolean validated() {
        if(lifecycles == null || lifecycles.isEmpty()){
            return false;
        }
        return lifecycles.stream().noneMatch(lifecycle ->
                Objects.isNull(lifecycle) ||
                        Objects.isNull(lifecycle.getId()) ||
                        Objects.isNull(lifecycle.getName()) ||
                        lifecycle.getId() < 1 ||
                        lifecycle.getName().isBlank());
    }

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }
}
