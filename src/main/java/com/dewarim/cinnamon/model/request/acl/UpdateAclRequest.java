package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateAclRequest")
public record UpdateAclRequest(
        @JacksonXmlElementWrapper(localName = "acls")
        @JacksonXmlProperty(localName = "acl")
        List<Acl> acls) implements UpdateRequest<Acl>, ApiRequest<UpdateAclRequest> {

    public UpdateAclRequest {
        if (acls == null) {
            acls = new ArrayList<>();
        }
    }

    public UpdateAclRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new Acl(id, name))));
    }

    @Override
    public List<Acl> list() {
        return acls;
    }

    @Override
    public boolean validated() {
        return acls.stream().allMatch(acl ->
                acl != null && acl.getName() != null && !acl.getName().trim().isEmpty()
                        && acl.getId() != null && acl.getId() > 0);
    }

    @Override
    public Wrapper<Acl> fetchResponseWrapper() {
        return new AclWrapper();
    }

    @Override
    public List<ApiRequest<UpdateAclRequest>> examples() {
        return List.of(new UpdateAclRequest(1L, "updated-name"));
    }
}
