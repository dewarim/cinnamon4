package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "addUserToGroupsRequest")
public class AddUserToGroupsRequest implements ApiRequest<AddUserToGroupsRequest> {

    @JacksonXmlElementWrapper(localName = "groupIds")
    @JacksonXmlProperty(localName = "groupId")
    private List<Long> groupIds;
    private Long       userId;

    public AddUserToGroupsRequest() {
    }

    public AddUserToGroupsRequest(Long userId, List<Long> groupIds) {
        this.groupIds = groupIds;
        this.userId = userId;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public Long getUserId() {
        return userId;
    }

    private boolean validated() {
        return groupIds != null && !groupIds.isEmpty() && groupIds.stream().allMatch(id -> id > 0)
                && userId != null && userId > 0;
    }

    public Optional<AddUserToGroupsRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<AddUserToGroupsRequest>> examples() {
        return List.of(new AddUserToGroupsRequest(33L, List.of(1L, 2L, 3L)));
    }
}
