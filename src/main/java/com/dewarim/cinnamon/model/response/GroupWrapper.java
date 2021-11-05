package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Group;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;


@JacksonXmlRootElement(localName = "cinnamon")
public class GroupWrapper implements Wrapper<Group>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    List<Group> groups = new ArrayList<>();

    public GroupWrapper() {
    }

    public GroupWrapper(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public List<Group> list() {
        return getGroups();
    }

    @Override
    public Wrapper<Group> setList(List<Group> groups) {
        setGroups(groups);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new GroupWrapper(List.of(new Group(1L,"group with parent", 2L), new Group(2L, "group without parent", null))));
    }
}
