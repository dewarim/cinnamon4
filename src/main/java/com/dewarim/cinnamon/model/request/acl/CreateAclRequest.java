package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createAclRequest")
public class CreateAclRequest implements CreateRequest<Acl>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "acls")
    @JacksonXmlProperty(localName = "acl")
    private List<Acl> acls = new ArrayList<>();

    @Override
    public List<Acl> list() {
        return acls;
    }

    public CreateAclRequest() {
    }

    public CreateAclRequest(String name) {
        acls.add(new Acl(name));
    }

    public CreateAclRequest(List<Acl> acls) {
        this.acls = acls;
    }


    @Override
    public boolean validated() {
        return acls.stream().noneMatch(acl ->
                acl == null || acl.getName() == null || acl.getName().trim().isEmpty());
    }

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }

    @Override
    public List<Object> examples() {
        return List.of(new CreateAclRequest(List.of(new Acl("default acl"), new Acl( "reviewers"), new Acl("authors"))));
    }
}
