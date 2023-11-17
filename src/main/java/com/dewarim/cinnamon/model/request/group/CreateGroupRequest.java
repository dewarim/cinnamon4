package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createGroupRequest")
public class CreateGroupRequest implements CreateRequest<Group>, ApiRequest<CreateGroupRequest> {

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    private List<Group> groups = new ArrayList<>();

    @Override
    public List<Group> list() {
        return groups;
    }

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(String name) {
        this.groups.add(new Group(name));
    }
    public CreateGroupRequest(String name, Long parentId) {
        this.groups.add(new Group(name, parentId));
    }

    public CreateGroupRequest(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
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
