package com.dewarim.cinnamon.model.request.permission;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listPermissionRequest")
public record ListPermissionRequest(ListType type) implements DefaultListRequest, ListRequest<Permission>, ApiRequest<ListPermissionRequest> {

    public ListPermissionRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListPermissionRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<Permission> fetchResponseWrapper() {
        return new PermissionWrapper();
    }

    @Override
    public List<ApiRequest<ListPermissionRequest>> examples() {
        return List.of(new ListPermissionRequest());
    }
}
