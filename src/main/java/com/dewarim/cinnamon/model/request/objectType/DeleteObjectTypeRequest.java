package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteObjectTypeRequest")
public record DeleteObjectTypeRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<ObjectType>, ApiRequest<DeleteObjectTypeRequest> {

    public DeleteObjectTypeRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteObjectTypeRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteObjectTypeRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteObjectTypeRequest>> examples() {
        return List.of(new DeleteObjectTypeRequest(List.of(2L, 3L)));
    }
}
