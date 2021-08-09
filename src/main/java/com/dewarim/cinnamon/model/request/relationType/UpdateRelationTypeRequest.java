package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateRelationTypeRequest")
public class UpdateRelationTypeRequest implements UpdateRequest<RelationType>, ApiRequest {

    private List<RelationType> types = new ArrayList<>();

    @Override
    public List<RelationType> list() {
        return types;
    }

    public UpdateRelationTypeRequest() {
    }

    public UpdateRelationTypeRequest(Long id, String name) {
    }

    public UpdateRelationTypeRequest(List<RelationType> RelationTypes) {
        this.types = RelationTypes;
    }

    public List<RelationType> getTypes() {
        return types;
    }

    @Override
    public boolean validated() {
        return types.stream().allMatch(type ->
            type != null && type.getName() != null && !type.getName().trim().isEmpty()
                    && type.getId() != null && type.getId() > 0);
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }
}
