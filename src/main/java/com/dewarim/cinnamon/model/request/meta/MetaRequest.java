package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("metaRequest")
public record MetaRequest(
        Long id,
        @JacksonXmlElementWrapper(localName = "typeIds")
        @JacksonXmlProperty(localName = "typeId")
        List<Long> typeIds) implements ApiRequest<MetaRequest> {

    public MetaRequest() {
        this(null, null);
    }

    private boolean validated() {
        return id != null && id > 0 && (typeIds == null || typeIds.stream().noneMatch(typeId -> typeId == null || typeId < 0));
    }

    public Optional<MetaRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<MetaRequest>> examples() {
        return List.of(new MetaRequest(3L, List.of(12L, 13L)), new MetaRequest(1L, null));
    }
}
