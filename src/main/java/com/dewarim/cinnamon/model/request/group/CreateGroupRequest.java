package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.GroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "createGroupRequest")
public class CreateGroupRequest implements CreateRequest<Group>, ApiRequest {

    private List<String> names = new ArrayList<>();

    @Override
    public List<Group> list() {
        return names.stream().map(Group::new).collect(Collectors.toList());
    }

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public boolean validated() {
        return names.stream().noneMatch(name -> name == null || name.trim().isEmpty());
    }

    @Override
    public Wrapper<Group> fetchResponseWrapper() {
        return new GroupWrapper();
    }
}
