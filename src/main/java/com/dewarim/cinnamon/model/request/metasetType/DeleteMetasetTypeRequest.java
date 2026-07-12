package com.dewarim.cinnamon.model.request.metasetType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteMetasetTypeRequest")
public record DeleteMetasetTypeRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<MetasetType>, ApiRequest<DeleteMetasetTypeRequest> {

    public DeleteMetasetTypeRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteMetasetTypeRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteMetasetTypeRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteMetasetTypeRequest>> examples() {
        return List.of(new DeleteMetasetTypeRequest(7L));
    }
}
