package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.MetasetTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listMetasetTypeRequest")
public record ListMetasetTypeRequest(ListType type) implements DefaultListRequest, ListRequest<MetasetType>, ApiRequest<ListMetasetTypeRequest> {

    public ListMetasetTypeRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListMetasetTypeRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<MetasetType> fetchResponseWrapper() {
        return new MetasetTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListMetasetTypeRequest>> examples() {
        return List.of(new ListMetasetTypeRequest());
    }
}
