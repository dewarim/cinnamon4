package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createGroupRequest")
public record CreateGroupRequest(
        @JacksonXmlElementWrapper(localName = "groups")
        @JacksonXmlProperty(localName = "group")
        List<Group> groups) implements CreateRequest<Group>, ApiRequest<CreateGroupRequest> {

    public CreateGroupRequest {
        if (groups == null) {
            groups = new ArrayList<>();
        }
    }

    public CreateGroupRequest(String name) {
        this(new ArrayList<>(List.of(new Group(name))));
    }

    public CreateGroupRequest(String name, Long parentId) {
        this(new ArrayList<>(List.of(new Group(name, parentId))));
    }

    @Override
    public List<Group> list() {
        return groups;
    }

    @Override
    public boolean validated() {
        return groups.stream().noneMatch(group -> group == null ||
                (group.getParentId() != null && group.getParentId() < 1) ||
                group.getName() == null ||
                group.getName().trim().isEmpty());
    }

    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }

    @Override
    public List<ApiRequest<CreateGroupRequest>> examples() {
        return List.of(new CreateGroupRequest(List.of(new Group("authors", 1L), new Group("reviewers", 1L), new Group("admins"))));
    }
}
