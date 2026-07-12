package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteRelationTypeRequest")
public record DeleteRelationTypeRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<RelationType>, ApiRequest<DeleteRelationTypeRequest> {

    public DeleteRelationTypeRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteRelationTypeRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteRelationTypeRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteRelationTypeRequest>> examples() {
        return List.of(new DeleteRelationTypeRequest(List.of(333L, 543L)));
    }
}
