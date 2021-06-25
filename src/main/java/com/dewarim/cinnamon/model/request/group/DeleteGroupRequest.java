package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteGroupRequest extends DeleteByIdRequest<Group> {

    public DeleteGroupRequest() {
    }

    public DeleteGroupRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteGroupRequest(Long id) {
        super(id);
    }
}
