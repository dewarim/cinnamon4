package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteLifecycleRequest")
public record DeleteLifecycleRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Lifecycle>, ApiRequest<DeleteLifecycleRequest> {

    public DeleteLifecycleRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteLifecycleRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteLifecycleRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteLifecycleRequest>> examples() {
        return List.of(new DeleteLifecycleRequest(1024L));
    }
}
