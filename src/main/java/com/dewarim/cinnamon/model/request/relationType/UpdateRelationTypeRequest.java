package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateRelationTypeRequest")
public record UpdateRelationTypeRequest(
        @JacksonXmlElementWrapper(localName = "relationTypes")
        @JacksonXmlProperty(localName = "relationType")
        @NotEmpty List<@Valid RelationType> relationTypes) implements UpdateRequest<RelationType>, ApiRequest<UpdateRelationTypeRequest> {

    public UpdateRelationTypeRequest {
        if (relationTypes == null) {
            relationTypes = new ArrayList<>();
        }
    }

    public UpdateRelationTypeRequest(Long id, String name) {
        this(new ArrayList<>());
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
    public List<ApiRequest<UpdateRelationTypeRequest>> examples() {
        return List.of(new UpdateRelationTypeRequest(List.of(new RelationType("updated-type", true, true, true, true, true, true))));
    }
}
