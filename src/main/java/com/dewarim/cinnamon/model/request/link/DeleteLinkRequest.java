package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteLinkRequest")
public record DeleteLinkRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Link>, ApiRequest<DeleteLinkRequest> {

    public DeleteLinkRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteLinkRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteLinkRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteLinkRequest>> examples() {
        return List.of(new DeleteLinkRequest(51L));
    }
}
