package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.AclGroupWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@JsonRootName("createAclGroupRequest")
public record CreateAclGroupRequest(
        @JacksonXmlElementWrapper(localName = "aclGroups")
        @JacksonXmlProperty(localName = "aclGroup")
        List<AclGroup> aclGroups) implements CreateRequest<AclGroup>, ApiRequest<CreateAclGroupRequest> {

    public CreateAclGroupRequest {
        if (aclGroups == null) {
            aclGroups = new ArrayList<>();
        }
    }

    @Override
    public List<AclGroup> list() {
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
    public List<ApiRequest<CreateAclGroupRequest>> examples() {
        AclGroup aclGroup = new AclGroup(1L, 2L);
        aclGroup.setPermissionIds(List.of(10L, 12L));
        return List.of(new CreateAclGroupRequest(List.of(aclGroup, new AclGroup(1L, 3L))));
    }
}
