package com.dewarim.cinnamon.model.request.acl;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteAclRequest")
public record DeleteAclRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Acl>, ApiRequest<DeleteAclRequest> {

    public DeleteAclRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteAclRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteAclRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteAclRequest>> examples() {
        return List.of(new DeleteAclRequest(List.of(43L, 99L)));
    }
}
