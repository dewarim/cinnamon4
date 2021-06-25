package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class CreateAclGroupRequest implements CreateRequest<AclGroup>, ApiRequest {

    private List<AclGroup> aclGroups = new ArrayList<>();

    @Override
    public List<AclGroup> list() {
        return aclGroups;
    }

    public CreateAclGroupRequest() {
    }

    public CreateAclGroupRequest(List<AclGroup> aclGroups) {
        this.aclGroups = aclGroups;
    }

    public List<AclGroup> getAclGroups() {
        return aclGroups;
    }

    @Override
    public boolean validated() {
        return aclGroups.stream().noneMatch(entry ->
             isNull(entry) || isNull(entry.getAclId()) || isNull(entry.getGroupId())
                    || entry.getAclId() < 1 || entry.getGroupId() < 1);
    }

    @Override
    public Wrapper<AclGroup> fetchResponseWrapper() {
        return new AclGroupWrapper();
    }

    @Override
    public Object example() {
        return new CreateAclGroupRequest(List.of(new AclGroup(1L,2L), new AclGroup(1L,3L)));
    }
}
