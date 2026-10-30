package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createRelationTypeRequest")
public record CreateRelationTypeRequest(
        @JacksonXmlElementWrapper(localName = "relationTypes")
        @JacksonXmlProperty(localName = "relationType")
        @NotEmpty List<@Valid RelationType> relationTypes) implements CreateRequest<RelationType>, ApiRequest<CreateRelationTypeRequest> {

    public CreateRelationTypeRequest {
        if (relationTypes == null) {
            relationTypes = new ArrayList<>();
        }
    }

    @Override
    public List<RelationType> list() {
        return relationTypes;
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }

    @Override
    public List<ApiRequest<CreateRelationTypeRequest>> examples() {
        return List.of(new CreateRelationTypeRequest(List.of(new RelationType("thumbnail-relation", true, false, true, false, true, false))));
    }
}
