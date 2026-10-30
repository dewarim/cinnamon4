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

@JsonRootName("addUserToGroupsRequest")
public record AddUserToGroupsRequest(
        @JacksonXmlElementWrapper(localName = "groupIds")
        @JacksonXmlProperty(localName = "groupId")
        @NotEmpty List<@NotNull @Positive Long> groupIds,
        @NotNull @Positive Long userId) implements ApiRequest<AddUserToGroupsRequest> {

    public AddUserToGroupsRequest(Long userId, List<Long> groupIds) {
        this(groupIds, userId);
    }

    public Optional<AddUserToGroupsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<AddUserToGroupsRequest>> examples() {
        return List.of(new AddUserToGroupsRequest(33L, List.of(1L, 2L, 3L)));
    }
}
