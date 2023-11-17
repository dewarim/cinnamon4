package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.model.Acl;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class AclWrapper implements Wrapper<Acl>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "acls")
    @JacksonXmlProperty(localName = "acl")
    List<Acl> acls = new ArrayList<>();

    public AclWrapper(List<Acl> acls) {
        this.acls = acls;
    }

    public AclWrapper() {
    }

    public void setAcls(List<Acl> acls) {
        this.acls = acls;
    }

    public List<Acl> getAcls() {
        return acls;
    }

    @Override
    public List<Acl> list() {
        return getAcls();
    }

    @Override
    public Wrapper<Acl> setList(List<Acl> acls) {
        setAcls(acls);
        return this;
    }

    @Override
    public List<Object> examples() {
        return List.of(new AclWrapper(List.of(new Acl(1L,"DEFAULT_ACL"), new Acl("reviewers"))));
    }
}
