package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteAclRequest extends DeleteByIdRequest<Acl> {

    public DeleteAclRequest() {
    }

    public DeleteAclRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteAclRequest(Long id) {
        super(id);
    }
}
