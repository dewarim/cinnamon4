package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteIndexItemRequest")
public record DeleteIndexItemRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<IndexItem>, ApiRequest<DeleteIndexItemRequest> {

    public DeleteIndexItemRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteIndexItemRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteIndexItemRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteIndexItemRequest>> examples() {
        return List.of(new DeleteIndexItemRequest(679L));
    }
}
