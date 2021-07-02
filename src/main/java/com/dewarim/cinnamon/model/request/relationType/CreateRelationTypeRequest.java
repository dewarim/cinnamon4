package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateRelationTypeRequest implements CreateRequest<RelationType>, ApiRequest {

    private List<RelationType> types = new ArrayList<>();

    @Override
    public List<RelationType> list() {
        return types;
    }

    public CreateRelationTypeRequest() {
    }

    public CreateRelationTypeRequest(List<RelationType> types) {
        this.types = types;
    }

    public List<RelationType> getTypes() {
        return types;
    }

    public void setTypes(List<RelationType> types) {
        this.types = types;
    }

    @Override
    public boolean validated() {
        return types.stream().allMatch(type ->
                Objects.nonNull(type) &&
                        Objects.nonNull(type.getName()) &&
                        !type.getName().isBlank()
        );
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }
}
