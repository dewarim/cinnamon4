package com.dewarim.cinnamon.model.request.aclEntry;

import com.dewarim.cinnamon.model.AclEntry;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteAclEntryRequest extends DeleteByIdRequest<AclEntry> {

    public DeleteAclEntryRequest() {
    }

    public DeleteAclEntryRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclEntryRequest(Long id) {
        super(id);
    }
}
