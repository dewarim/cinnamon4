package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteLifecycleStateRequest")
public record DeleteLifecycleStateRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<LifecycleState>, ApiRequest<DeleteLifecycleStateRequest> {

    public DeleteLifecycleStateRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteLifecycleStateRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteLifecycleStateRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteLifecycleStateRequest>> examples() {
        return List.of(new DeleteLifecycleStateRequest(6L));
    }
}
