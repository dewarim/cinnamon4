package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.AclGroup;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class AclGroupWrapper extends BaseResponse implements Wrapper<AclGroup>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "aclGroups")
    @JacksonXmlProperty(localName = "aclGroup")
    List<AclGroup> aclGroups;

    public AclGroupWrapper(List<AclGroup> aclGroups) {
        this.aclGroups = aclGroups;
    }

    public AclGroupWrapper() {
        this.aclGroups = new ArrayList<>();
    }

    public List<AclGroup> getAclGroups() {
        return aclGroups;
    }

    public void setAclGroups(List<AclGroup> aclGroups) {
        this.aclGroups = aclGroups;
    }

    @Override
    public List<AclGroup> list() {
        return getAclGroups();
    }

    @Override
    public Wrapper<AclGroup> setList(List<AclGroup> aclGroups) {
        setAclGroups(aclGroups);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(
                new AclGroupWrapper(List.of(new AclGroup(1L, 1L, 2L), new AclGroup(2L, 1L, 3L))));
    }
}
