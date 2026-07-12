package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listRelationTypeRequest")
public record ListRelationTypeRequest(ListType type) implements DefaultListRequest, ListRequest<RelationType>, ApiRequest<ListRelationTypeRequest> {

    public ListRelationTypeRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListRelationTypeRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListRelationTypeRequest>> examples() {
        return List.of(new ListRelationTypeRequest());
    }
}
