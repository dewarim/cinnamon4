package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listGroupRequest")
public record ListGroupRequest(ListType type) implements DefaultListRequest, ListRequest<Group>, ApiRequest<ListGroupRequest> {

    public ListGroupRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListGroupRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }

    @Override
    public List<ApiRequest<ListGroupRequest>> examples() {
        return List.of(new ListGroupRequest());
    }
}
