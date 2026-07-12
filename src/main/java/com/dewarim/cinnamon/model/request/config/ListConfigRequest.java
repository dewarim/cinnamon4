package com.dewarim.cinnamon.model.request.config;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listConfigRequest")
public record ListConfigRequest(ListType type) implements DefaultListRequest, ApiRequest<ListConfigRequest> {

    public ListConfigRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListConfigRequest() {
        this(ListType.FULL);
    }
    @Override
    public List<ApiRequest<ListConfigRequest>> examples() {
        return List.of(new ListConfigRequest());
    }
}
