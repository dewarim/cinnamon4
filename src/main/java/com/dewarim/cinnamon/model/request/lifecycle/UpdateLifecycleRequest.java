package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("updateLifecycleRequest")
public record UpdateLifecycleRequest(
        @JacksonXmlElementWrapper(localName = "lifecycles")
        @JacksonXmlProperty(localName = "lifecycle")
        List<Lifecycle> lifecycles) implements UpdateRequest<Lifecycle>, ApiRequest<UpdateLifecycleRequest> {

    public UpdateLifecycleRequest {
        if (lifecycles == null) {
            lifecycles = new ArrayList<>();
        }
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
                        Objects.isNull(lifecycle.getId()) ||
                        Objects.isNull(lifecycle.getName()) ||
                        lifecycle.getId() < 1 ||
                        lifecycle.getName().isBlank());
    }

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }

    @Override
    public List<ApiRequest<UpdateLifecycleRequest>> examples() {
        return List.of(new UpdateLifecycleRequest(List.of(new Lifecycle("my new LC", 5L))));
    }
}
