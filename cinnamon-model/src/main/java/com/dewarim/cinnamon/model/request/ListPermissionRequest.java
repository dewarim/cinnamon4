package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.response.PermissionWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListPermissionRequest extends DefaultListRequest implements ListRequest<Permission>{
    @Override
    public Wrapper<Permission> fetchResponseWrapper() {
        return new PermissionWrapper();
    }
}
