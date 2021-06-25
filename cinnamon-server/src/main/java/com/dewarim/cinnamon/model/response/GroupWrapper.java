package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.Group;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;


@JacksonXmlRootElement(localName = "cinnamon")
public class GroupWrapper implements Wrapper<Group>{

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    List<Group> groups = new ArrayList<>();

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
}
