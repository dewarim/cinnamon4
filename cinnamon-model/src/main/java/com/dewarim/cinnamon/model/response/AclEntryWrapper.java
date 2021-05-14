package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.model.AclEntry;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class AclEntryWrapper implements Wrapper<AclEntry>{

    @JacksonXmlElementWrapper(localName = "aclEntries")
    @JacksonXmlProperty(localName = "aclEntry")
    List<AclEntry> aclEntries;

    public AclEntryWrapper(List<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }

    public AclEntryWrapper() {
        this.aclEntries = new ArrayList<>();
    }

    public List<AclEntry> getAclEntries() {
        return aclEntries;
    }

    public void setAclEntries(List<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }

    @Override
    public List<AclEntry> list() {
        return getAclEntries();
    }

    @Override
    public Wrapper<AclEntry> setList(List<AclEntry> aclEntries) {
        setAclEntries(aclEntries);
        return this;
    }
}
