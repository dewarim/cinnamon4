package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.UrlMappingInfo;
import com.dewarim.cinnamon.model.response.UrlMappingInfoWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listUrlMappingInfoRequest")

public record ListUrlMappingInfoRequest(ListType type) implements DefaultListRequest, ListRequest<UrlMappingInfo>, ApiRequest<ListUrlMappingInfoRequest> {

    public ListUrlMappingInfoRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListUrlMappingInfoRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<UrlMappingInfo> fetchResponseWrapper() {
        return new UrlMappingInfoWrapper();
    }

    @Override
    public List<ApiRequest<ListUrlMappingInfoRequest>> examples() {
        return List.of(new ListUrlMappingInfoRequest());
    }
}
