package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listAclRequest")
public record ListAclRequest(ListType type) implements DefaultListRequest, ListRequest<Acl>, ApiRequest<ListAclRequest> {

    public ListAclRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListAclRequest() {
        this(ListType.FULL);
    }

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }

    @Override
    public List<ApiRequest<ListAclRequest>> examples() {
        return List.of(new ListAclRequest());
    }

}
