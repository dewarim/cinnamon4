package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ConfigEntry;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteConfigRequest")
public record DeleteConfigEntryRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<ConfigEntry>, ApiRequest<DeleteByIdRequest<ConfigEntry>> {

    public DeleteConfigEntryRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteConfigEntryRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteConfigEntryRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteByIdRequest<ConfigEntry>>> examples() {
        return List.of(new DeleteConfigEntryRequest(List.of(1L, 2L)));
    }
}
