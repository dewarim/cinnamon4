package com.dewarim.cinnamon.model.request.format;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteFormatRequest")
public record DeleteFormatRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<Format>, ApiRequest<DeleteFormatRequest> {

    public DeleteFormatRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteFormatRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteFormatRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteFormatRequest>> examples() {
        return List.of(new DeleteFormatRequest(999L));
    }
}
