package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createAclRequest")
public record CreateAclRequest(
        @JacksonXmlElementWrapper(localName = "acls")
        @JacksonXmlProperty(localName = "acl")
        List<Acl> acls) implements CreateRequest<Acl>, ApiRequest<CreateAclRequest> {

    public CreateAclRequest {
        if (acls == null) {
            acls = new ArrayList<>();
        }
    }

    public CreateAclRequest(String name) {
        this(new ArrayList<>(List.of(new Acl(name))));
    }

    @Override
    public List<Acl> list() {
        return acls;
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
    public List<ApiRequest<CreateAclRequest>> examples() {
        return List.of(new CreateAclRequest(List.of(new Acl("default acl"), new Acl("reviewers"), new Acl("authors"))));
    }
}
