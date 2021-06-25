package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

public class UpdateAclRequest implements UpdateRequest<Acl> {

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
}
