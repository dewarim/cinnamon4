package com.dewarim.cinnamon.model.request.relation;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "createRelationRequest")
public class CreateRelationRequest implements CreateRequest<Relation>, ApiRequest<CreateRelationRequest> {

    @JacksonXmlElementWrapper(localName = "relations")
    @JacksonXmlProperty(localName = "relation")
    private List<Relation> relations = new ArrayList<>();

    public CreateRelationRequest(List<Relation> relations) {
        this.relations = relations;
    }

    public CreateRelationRequest(Long leftId, Long rightId, Long typeId, String metadata) {
        relations.add(new Relation(leftId, rightId, typeId, metadata));
    }

    public CreateRelationRequest() {
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
