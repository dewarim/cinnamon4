package com.dewarim.cinnamon.model.request.lifecycleState;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.LifecycleStateWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listLifecycleStateRequest")
public record ListLifecycleStateRequest(ListType type) implements DefaultListRequest, ListRequest<LifecycleState>, ApiRequest<ListLifecycleStateRequest> {

    public ListLifecycleStateRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListLifecycleStateRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<LifecycleState> fetchResponseWrapper() {
        return new LifecycleStateWrapper();
    }

    @Override
    public List<ApiRequest<ListLifecycleStateRequest>> examples() {
        return List.of(new ListLifecycleStateRequest());
    }
}
