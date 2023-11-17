package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listAclRequest")
public class ListAclRequest extends DefaultListRequest implements ListRequest<Acl>, ApiRequest<ListAclRequest> {

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }

    @Override
    public List<ApiRequest<ListAclRequest>> examples() {
        return List.of(new ListAclRequest());
    }

}
