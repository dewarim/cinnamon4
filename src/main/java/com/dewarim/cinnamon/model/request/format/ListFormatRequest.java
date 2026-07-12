package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FormatWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listFormatRequest")
public record ListFormatRequest(ListType type) implements DefaultListRequest, ListRequest<Format>, ApiRequest<ListFormatRequest> {

    public ListFormatRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListFormatRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<Format> fetchResponseWrapper() {
        return new FormatWrapper();
    }

    @Override
    public List<ApiRequest<ListFormatRequest>> examples() {
        return List.of(new ListFormatRequest());
    }
}
