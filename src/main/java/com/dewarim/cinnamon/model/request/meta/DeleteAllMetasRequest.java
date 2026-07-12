package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@JsonRootName("deleteAllMetasRequest")
public record DeleteAllMetasRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Meta>, ApiRequest<DeleteAllMetasRequest> {

    public DeleteAllMetasRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteAllMetasRequest(Set<Long> ids) {
        this(ids, false);
    }

    public DeleteAllMetasRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteAllMetasRequest>> examples() {
        return List.of(new DeleteAllMetasRequest(new TreeSet<>(List.of(14L, 15L))));
    }
}
