package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.Acl;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class AclWrapper {

    @JacksonXmlElementWrapper(localName = "acls")
    @JacksonXmlProperty(localName = "acl")
    List<Acl> acls = new ArrayList<>();

    public void setAcls(List<Acl> acls) {
        this.acls = acls;
    }

    public List<Acl> getAcls() {
        return acls;
    }
}
