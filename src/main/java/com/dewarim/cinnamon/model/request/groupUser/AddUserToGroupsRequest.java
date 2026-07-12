package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("addUserToGroupsRequest")
public record AddUserToGroupsRequest(
        @JacksonXmlElementWrapper(localName = "groupIds")
        @JacksonXmlProperty(localName = "groupId")
        List<Long> groupIds,
        Long userId) implements ApiRequest<AddUserToGroupsRequest> {

    public AddUserToGroupsRequest(Long userId, List<Long> groupIds) {
        this(groupIds, userId);
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
