package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("createLifecycleRequest")
public record CreateLifecycleRequest(
        @JacksonXmlElementWrapper(localName = "lifecycles")
        @JacksonXmlProperty(localName = "lifecycle")
        List<Lifecycle> lifecycles) implements CreateRequest<Lifecycle>, ApiRequest<Lifecycle> {

    public CreateLifecycleRequest {
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
                        Objects.isNull(lifecycle.getName()) ||
                        (!Objects.isNull(lifecycle.getDefaultStateId()) && lifecycle.getDefaultStateId() < 1L) ||
                        lifecycle.getName().isBlank()
        );
    }

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }

    @Override
    public List<ApiRequest<Lifecycle>> examples() {
        return List.of(new CreateLifecycleRequest(List.of(new Lifecycle("authoring", null), new Lifecycle("translation-lc", null))));
    }
}
