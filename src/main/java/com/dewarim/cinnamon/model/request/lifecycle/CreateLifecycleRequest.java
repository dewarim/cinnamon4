package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createLifecycleRequest")
public record CreateLifecycleRequest(
        @JacksonXmlElementWrapper(localName = "lifecycles")
        @JacksonXmlProperty(localName = "lifecycle")
        @NotEmpty List<@Valid Lifecycle> lifecycles) implements CreateRequest<Lifecycle>, ApiRequest<Lifecycle> {

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
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }

    @Override
    public List<ApiRequest<Lifecycle>> examples() {
        return List.of(new CreateLifecycleRequest(List.of(new Lifecycle("authoring", null), new Lifecycle("translation-lc", null))));
    }
}
