package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateGroupRequest")
public class UpdateGroupRequest implements UpdateRequest<Group>, ApiRequest<UpdateGroupRequest> {

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    private List<Group> groups = new ArrayList<>();

    @Override
    public List<Group> list() {
        return groups;
    }

    public UpdateGroupRequest() {
    }

    public UpdateGroupRequest(Long id, String name) {
        groups.add(new Group(name));
    }

    public UpdateGroupRequest(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    @Override
    public boolean validated() {
        return groups.stream().allMatch(group ->
            group != null && group.getName() != null && !group.getName().trim().isEmpty()
                    && group.getId() != null && group.getId() > 0);
    }

    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }

    @Override
    public List<ApiRequest<UpdateGroupRequest>> examples() {
        return List.of(new UpdateGroupRequest(List.of(new Group(11L, "updated-group-name",2L))));
    }
}
