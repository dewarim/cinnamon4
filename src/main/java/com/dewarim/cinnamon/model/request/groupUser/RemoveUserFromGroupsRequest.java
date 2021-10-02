package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "removeUserFromGroupsRequest")
public class RemoveUserFromGroupsRequest implements ApiRequest {

    private List<Long> ids;
    private Long       userId;

    public RemoveUserFromGroupsRequest() {
    }

    public RemoveUserFromGroupsRequest(Long userId, List<Long> ids) {
        this.userId = userId;
        this.ids = ids;
    }

    public List<Long> getIds() {
        return ids;
    }

    public Long getUserId() {
        return userId;
    }

    private boolean validated() {
        return userId != null && userId > 0 && ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id > 0);
    }

    public Optional<RemoveUserFromGroupsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
