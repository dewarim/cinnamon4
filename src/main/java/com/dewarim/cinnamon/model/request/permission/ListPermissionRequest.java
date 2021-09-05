package com.dewarim.cinnamon.model.request.permission;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "listPermissionRequest")
public class ListPermissionRequest extends DefaultListRequest implements ListRequest<Permission>, ApiRequest {
    @Override
    public Wrapper<Permission> fetchResponseWrapper() {
        return new PermissionWrapper();
    }
}
