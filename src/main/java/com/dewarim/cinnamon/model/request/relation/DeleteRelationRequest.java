package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteRelationRequest")
public record DeleteRelationRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Relation>, ApiRequest<DeleteRelationRequest> {

    public DeleteRelationRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteRelationRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteRelationRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteRelationRequest>> examples() {
        return List.of(new DeleteRelationRequest(68L));
    }
}
