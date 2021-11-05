package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "createRelationTypeRequest")
public class CreateRelationTypeRequest implements CreateRequest<RelationType>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "relationTypes")
    @JacksonXmlProperty(localName = "relationType")
    private List<RelationType> relationTypes = new ArrayList<>();

    @Override
    public List<RelationType> list() {
        return relationTypes;
    }

    public CreateRelationTypeRequest() {
    }

    public CreateRelationTypeRequest(List<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    public List<RelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<RelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }

    @Override
    public boolean validated() {
        return relationTypes.stream().allMatch(type ->
                Objects.nonNull(type) &&
                        Objects.nonNull(type.getName()) &&
                        !type.getName().isBlank()
        );
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateRelationTypeRequest(List.of(new RelationType("thumbnail-relation", true, false, true, false, true, false))));
    }
}
