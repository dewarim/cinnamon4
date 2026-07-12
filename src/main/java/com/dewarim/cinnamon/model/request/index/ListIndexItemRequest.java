package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.IndexItem;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.IndexItemWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listIndexItemRequest")
public record ListIndexItemRequest(ListType type) implements DefaultListRequest, ListRequest<IndexItem>, ApiRequest<ListIndexItemRequest> {

    public ListIndexItemRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListIndexItemRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<IndexItem> fetchResponseWrapper() {
        return new IndexItemWrapper();
    }

    @Override
    public List<ApiRequest<ListIndexItemRequest>> examples() {
        return List.of(new ListIndexItemRequest());
    }
}
