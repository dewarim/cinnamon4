package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteMetaRequest")
public record DeleteMetaRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Meta>, ApiRequest<DeleteMetaRequest> {

    public DeleteMetaRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteMetaRequest() {
        this(new HashSet<>(), false);
    }

    public DeleteMetaRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteMetaRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    public DeleteMetaRequest(Long id, boolean ignoreNotFound) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), ignoreNotFound);
    }

    @Override
    public List<ApiRequest<DeleteMetaRequest>> examples() {
        return List.of(new DeleteMetaRequest(List.of(3L, 5L, 6L)), new DeleteMetaRequest(1L, true));
    }
}
