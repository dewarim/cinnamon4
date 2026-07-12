package com.dewarim.cinnamon.model.request.lifecycle;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Lifecycle;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listLifecycleRequest")
public record ListLifecycleRequest(ListType type) implements DefaultListRequest, ListRequest<Lifecycle>, ApiRequest<ListLifecycleRequest> {

    public ListLifecycleRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListLifecycleRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<Lifecycle> fetchResponseWrapper() {
        return new LifecycleWrapper();
    }

    @Override
    public List<ApiRequest<ListLifecycleRequest>> examples() {
        return List.of(new ListLifecycleRequest());
    }
}
