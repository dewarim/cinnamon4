package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listAclGroupRequest")
public class ListAclGroupRequest extends DefaultListRequest implements ListRequest<AclGroup>, ApiRequest<ListAclGroupRequest> {
    @Override
    public Wrapper<AclGroup> fetchResponseWrapper() {
        return new AclGroupWrapper();
    }

    @Override
    public List<ApiRequest<ListAclGroupRequest>> examples() {
        return List.of(new ListAclGroupRequest());
    }
}
