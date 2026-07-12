package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.ConfigEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listConfigEntryRequest")
public record ListConfigEntryRequest(ListType type) implements DefaultListRequest, ListRequest<ConfigEntry>, ApiRequest<ListConfigEntryRequest> {

    public ListConfigEntryRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListConfigEntryRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<ConfigEntry> fetchResponseWrapper() {
        return new ConfigEntryWrapper();
    }

    @Override
    public List<ApiRequest<ListConfigEntryRequest>> examples() {
        return List.of(new ListConfigEntryRequest());
    }
}
