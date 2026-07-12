package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonRootName("createRelationRequest")
public record CreateRelationRequest(
        @JacksonXmlElementWrapper(localName = "relations")
        @JacksonXmlProperty(localName = "relation")
        List<Relation> relations) implements CreateRequest<Relation>, ApiRequest<CreateRelationRequest> {

    public CreateRelationRequest {
        if (relations == null) {
            relations = new ArrayList<>();
        }
    }

    public CreateRelationRequest(Long leftId, Long rightId, Long typeId, String metadata) {
        this(new ArrayList<>(List.of(new Relation(leftId, rightId, typeId, metadata))));
    }

    public boolean validated() {
        return Objects.nonNull(relations) && relations.stream().noneMatch(r -> r == null ||
                r.getLeftId() == null ||
                r.getRightId() == null ||
                r.getTypeId() == null ||
                r.getLeftId() <= 0 || r.getRightId() <= 0 && r.getTypeId() <= 0);
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
