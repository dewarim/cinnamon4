package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateGroupRequest")
public record UpdateGroupRequest(
        @JacksonXmlElementWrapper(localName = "groups")
        @JacksonXmlProperty(localName = "group")
        @NotEmpty List<@Valid Group> groups) implements UpdateRequest<Group>, ApiRequest<UpdateGroupRequest> {

    public UpdateGroupRequest {
        if (groups == null) {
            groups = new ArrayList<>();
        }
    }

    public UpdateGroupRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new Group(name))));
    }

    @Override
    public List<Group> list() {
        return groups;
    }

    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }

    @Override
    public List<ApiRequest<UpdateGroupRequest>> examples() {
        return List.of(new UpdateGroupRequest(List.of(new Group(11L, "updated-group-name", 2L))));
    }
}
