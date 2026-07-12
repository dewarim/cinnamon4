package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listAclGroupRequest")
public record ListAclGroupRequest(ListType type) implements DefaultListRequest, ListRequest<AclGroup>, ApiRequest<ListAclGroupRequest> {

    public ListAclGroupRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListAclGroupRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<AclGroup> fetchResponseWrapper() {
        return new AclGroupWrapper();
    }

    @Override
    public List<ApiRequest<ListAclGroupRequest>> examples() {
        return List.of(new ListAclGroupRequest());
    }
}
