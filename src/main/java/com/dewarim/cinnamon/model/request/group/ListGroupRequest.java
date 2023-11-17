package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listGroupRequest")
public class ListGroupRequest extends DefaultListRequest implements ListRequest<Group>, ApiRequest<ListGroupRequest> {
    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }

    @Override
    public List<ApiRequest<ListGroupRequest>> examples() {
        return List.of(new ListGroupRequest());
    }
}
