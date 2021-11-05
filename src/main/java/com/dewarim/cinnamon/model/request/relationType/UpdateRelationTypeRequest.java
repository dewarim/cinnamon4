package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateRelationTypeRequest")
public class UpdateRelationTypeRequest implements UpdateRequest<RelationType>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "relationTypes")
    @JacksonXmlProperty(localName = "relationType")
    private List<RelationType> relationTypes = new ArrayList<>();

    @Override
    public List<RelationType> list() {
        return relationTypes;
    }

    public UpdateRelationTypeRequest() {
    }

    public UpdateRelationTypeRequest(Long id, String name) {
    }

    public UpdateRelationTypeRequest(List<RelationType> RelationTypes) {
        this.relationTypes = RelationTypes;
    }

    public List<RelationType> getRelationTypes() {
        return relationTypes;
    }

    @Override
    public boolean validated() {
        return relationTypes.stream().allMatch(type ->
            type != null && type.getName() != null && !type.getName().trim().isEmpty()
                    && type.getId() != null && type.getId() > 0);
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new UpdateRelationTypeRequest(List.of(new RelationType("updated-type",true,true,true,true,true,true))));
    }
}
