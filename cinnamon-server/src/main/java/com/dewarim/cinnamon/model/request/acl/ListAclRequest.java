package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListAclRequest extends DefaultListRequest implements ListRequest<Acl> {

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }
}
