package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@JsonRootName("updateAclGroupRequest")
public record UpdateAclGroupRequest(
        @JacksonXmlElementWrapper(localName = "aclGroups")
        @JacksonXmlProperty(localName = "aclGroup")
        List<AclGroup> aclGroups) implements UpdateRequest<AclGroup>, ApiRequest<UpdateAclGroupRequest> {

    public UpdateAclGroupRequest {
        if (aclGroups == null) {
            aclGroups = new ArrayList<>();
        }
    }

    public UpdateAclGroupRequest(Long id, Long aclId, Long groupId) {
        this(new ArrayList<>(List.of(new AclGroup(id, aclId, groupId))));
    }

    @Override
    public List<AclGroup> list() {
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
    public List<ApiRequest<UpdateAclGroupRequest>> examples() {
        AclGroup aclGroup = new AclGroup(1345L, 54L, 4L);
        aclGroup.getPermissionIds().add(5L);
        aclGroup.getPermissionIds().add(2L);
        return List.of(new UpdateAclGroupRequest(List.of(aclGroup)));
    }
}
