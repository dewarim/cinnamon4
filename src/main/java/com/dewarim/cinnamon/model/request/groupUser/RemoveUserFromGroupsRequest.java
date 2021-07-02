package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.List;
import java.util.Optional;

public class RemoveUserFromGroupsRequest implements ApiRequest {

    private List<Long> ids;

    public RemoveUserFromGroupsRequest() {
    }

    public RemoveUserFromGroupsRequest(List<Long> ids) {
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    private boolean validated(){
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id > 0);
    }

    public Optional<RemoveUserFromGroupsRequest> validateRequest() {
    if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
