package com.dewarim.cinnamon.model.request.groupUser;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("removeUserFromGroupsRequest")
public record RemoveUserFromGroupsRequest(
        @JacksonXmlElementWrapper(localName = "groupIds")
        @JacksonXmlProperty(localName = "groupId")
        @NotEmpty List<@NotNull @Positive Long> groupIds,
        @NotNull @Positive Long userId) implements ApiRequest<RemoveUserFromGroupsRequest> {

    public RemoveUserFromGroupsRequest(Long userId, List<Long> groupIds) {
        this(groupIds, userId);
    }

    public Optional<RemoveUserFromGroupsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<RemoveUserFromGroupsRequest>> examples() {
        return List.of(new RemoveUserFromGroupsRequest(33L, List.of(1L, 2L, 3L)));
    }
}
