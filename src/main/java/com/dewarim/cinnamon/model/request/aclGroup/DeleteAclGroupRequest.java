package com.dewarim.cinnamon.model.request.aclGroup;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteAclGroupRequest")
public record DeleteAclGroupRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<AclGroup>, ApiRequest<DeleteAclGroupRequest> {

    public DeleteAclGroupRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteAclGroupRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteAclGroupRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteAclGroupRequest>> examples() {
        return List.of(new DeleteAclGroupRequest(List.of(5L, 78L)));
    }
}
