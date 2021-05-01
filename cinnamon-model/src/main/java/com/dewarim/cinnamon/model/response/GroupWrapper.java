package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.CmnGroup;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;


@JacksonXmlRootElement(localName = "cinnamon")
public class GroupWrapper implements Wrapper<CmnGroup>{

    @JacksonXmlElementWrapper(localName = "groups")
    @JacksonXmlProperty(localName = "group")
    List<CmnGroup> groups = new ArrayList<>();

    public List<CmnGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<CmnGroup> groups) {
        this.groups = groups;
    }

    @Override
    public List<CmnGroup> list() {
        return getGroups();
    }

    @Override
    public Wrapper<CmnGroup> setList(List<CmnGroup> cmnGroups) {
        setGroups(cmnGroups);
        return this;
    }
}
