package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateAclRequest")
public class UpdateAclRequest implements UpdateRequest<Acl>, ApiRequest<UpdateAclRequest> {

    @JacksonXmlElementWrapper(localName = "acls")
    @JacksonXmlProperty(localName = "acl")
    private List<Acl> acls = new ArrayList<>();

    @Override
    public List<Acl> list() {
        return acls;
    }

    public UpdateAclRequest() {
    }

    public UpdateAclRequest(Long id, String name) {
        acls.add(new Acl(id,name));
    }

    public UpdateAclRequest(List<Acl> acls) {
        this.acls = acls;
    }

    public List<Acl> getAcls() {
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
