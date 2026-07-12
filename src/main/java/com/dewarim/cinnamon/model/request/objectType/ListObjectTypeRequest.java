package com.dewarim.cinnamon.model.request.objectType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ObjectTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listObjectTypeRequest")
public record ListObjectTypeRequest(ListType type) implements DefaultListRequest, ListRequest<ObjectType>, ApiRequest<ListObjectTypeRequest> {

    public ListObjectTypeRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListObjectTypeRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<ObjectType> fetchResponseWrapper() {
        return new ObjectTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListObjectTypeRequest>> examples() {
        return List.of(new ListObjectTypeRequest());
    }
}
