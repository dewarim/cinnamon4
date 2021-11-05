package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "removeUserFromGroupsRequest")
public class RemoveUserFromGroupsRequest implements ApiRequest {

    @JacksonXmlElementWrapper(localName = "groupIds")
    @JacksonXmlProperty(localName = "groupId")
    private List<Long> groupIds;
    private Long       userId;

    public RemoveUserFromGroupsRequest() {
    }

    public RemoveUserFromGroupsRequest(Long userId, List<Long> groupIds) {
        this.userId = userId;
        this.groupIds = groupIds;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public Long getUserId() {
        return userId;
    }

    private boolean validated() {
        return userId != null && userId > 0 && groupIds != null && !groupIds.isEmpty() && groupIds.stream().allMatch(id -> id > 0);
    }

    public Optional<RemoveUserFromGroupsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Object> examples() {
        return List.of(new RemoveUserFromGroupsRequest(33L, List.of(1L, 2L, 3L)));
    }
}
