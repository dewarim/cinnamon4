package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createRelationRequest")
public record CreateRelationRequest(
        @JacksonXmlElementWrapper(localName = "relations")
        @JacksonXmlProperty(localName = "relation")
        @NotEmpty List<@Valid Relation> relations) implements CreateRequest<Relation>, ApiRequest<CreateRelationRequest> {

    public CreateRelationRequest {
        if (relations == null) {
            relations = new ArrayList<>();
        }
    }

    public CreateRelationRequest(Long leftId, Long rightId, Long typeId, String metadata) {
        this(new ArrayList<>(List.of(new Relation(leftId, rightId, typeId, metadata))));
    }

    @Override
    public List<Relation> list() {
        return relations;
    }

    @Override
    public Wrapper<Relation> fetchResponseWrapper() {
        return new RelationWrapper();
    }

    @Override
    public List<ApiRequest<CreateRelationRequest>> examples() {
        return List.of(new CreateRelationRequest(1L, 2L, 3L, "<meta/>"), new CreateRelationRequest(2L, 1L, 10L, "<xml>test</xml>"));
    }
}
