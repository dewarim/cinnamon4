package com.dewarim.cinnamon.model.request.groupUser;

import java.util.List;
import java.util.Optional;

public class AddUserToGroupsRequest {
    private List<Long> ids;

    public AddUserToGroupsRequest() {
    }

    public AddUserToGroupsRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    private boolean validated(){
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id > 0);
    }

    public Optional<AddUserToGroupsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
