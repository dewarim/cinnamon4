package com.dewarim.cinnamon.model.request.aclEntry;

import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.AclEntryWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListAclEntryRequest extends DefaultListRequest implements ListRequest<AclEntry> {
    @Override
    public Wrapper<AclEntry> fetchResponseWrapper() {
        return new AclEntryWrapper();
    }
}
