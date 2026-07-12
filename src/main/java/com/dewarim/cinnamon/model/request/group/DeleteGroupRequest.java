package com.dewarim.cinnamon.model.request.group;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JsonRootName("deleteGroupRequest")
public record DeleteGroupRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound,
        boolean recursive) implements DeleteByIdRequest<Group>, ApiRequest<DeleteGroupRequest> {

    public DeleteGroupRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteGroupRequest(List<Long> ids) {
        this(new HashSet<>(ids), false, false);
    }

    public DeleteGroupRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false, false);
    }

    public DeleteGroupRequest(List<Long> ids, boolean recursive) {
        this(new HashSet<>(ids), false, recursive);
    }

    public Optional<DeleteGroupRequest> validate() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<DeleteGroupRequest>> examples() {
        return List.of(new DeleteGroupRequest(List.of(4L, 6L, 7L), true));
    }
}
