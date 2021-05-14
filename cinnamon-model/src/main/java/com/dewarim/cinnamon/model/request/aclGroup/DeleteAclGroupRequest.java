package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteAclGroupRequest extends DeleteByIdRequest<AclGroup> {

    public DeleteAclGroupRequest() {
    }

    public DeleteAclGroupRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclGroupRequest(Long id) {
        super(id);
    }
}
