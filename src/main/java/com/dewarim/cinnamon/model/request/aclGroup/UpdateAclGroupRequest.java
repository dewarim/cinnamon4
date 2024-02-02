package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@JacksonXmlRootElement(localName = "updateAclGroupRequest")
public class UpdateAclGroupRequest implements UpdateRequest<AclGroup>, ApiRequest<UpdateAclGroupRequest> {

    @JacksonXmlElementWrapper(localName = "aclGroups")
    @JacksonXmlProperty(localName = "aclGroup")
    private List<AclGroup> aclGroups = new ArrayList<>();

    @Override
    public List<AclGroup> list() {
        return aclGroups;
    }

    public UpdateAclGroupRequest() {
    }

    public UpdateAclGroupRequest(Long id, Long aclId, Long groupId) {
        aclGroups.add(new AclGroup(id, aclId, groupId));
    }

    public UpdateAclGroupRequest(List<AclGroup> aclGroups) {
        this.aclGroups = aclGroups;
    }

    public List<AclGroup> getAclGroups() {
        return aclGroups;
    }

    @Override
    public boolean validated() {
        return aclGroups.stream().noneMatch(entry ->
                isNull(entry) || isNull(entry.getAclId()) || isNull(entry.getGroupId())
                        || isNull(entry.getId())
                        || entry.getAclId() < 1 || entry.getGroupId() < 1 || entry.getId() < 1);
    }

    @Override
    public Wrapper<AclGroup> fetchResponseWrapper() {
        return new AclGroupWrapper();
    }

    @Override
    public String toString() {
        return "UpdateAclGroupRequest{" +
                "aclGroups=" + aclGroups +
                '}';
    }

    @Override
    public List<ApiRequest<UpdateAclGroupRequest>> examples() {
        AclGroup aclGroup = new AclGroup(1345L, 54L, 4L);
        aclGroup.getPermissionIds().add(5L);
        aclGroup.getPermissionIds().add(2L);
        return List.of(new UpdateAclGroupRequest(aclGroups));
    }
}
