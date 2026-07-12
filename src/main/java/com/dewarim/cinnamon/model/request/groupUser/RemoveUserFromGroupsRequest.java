package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("removeUserFromGroupsRequest")
public record RemoveUserFromGroupsRequest(
        @JacksonXmlElementWrapper(localName = "groupIds")
        @JacksonXmlProperty(localName = "groupId")
        List<Long> groupIds,
        Long userId) implements ApiRequest<RemoveUserFromGroupsRequest> {

    public RemoveUserFromGroupsRequest(Long userId, List<Long> groupIds) {
        this(groupIds, userId);
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
    public List<ApiRequest<RemoveUserFromGroupsRequest>> examples() {
        return List.of(new RemoveUserFromGroupsRequest(33L, List.of(1L, 2L, 3L)));
    }
}
