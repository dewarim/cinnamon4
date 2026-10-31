package com.dewarim.cinnamon.model.request.meta;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("metaRequest")
public record MetaRequest(
        @NotNull @Positive Long id,
        @JacksonXmlElementWrapper(localName = "typeIds")
        @JacksonXmlProperty(localName = "typeId")
        List<@NotNull @Positive Long> typeIds) implements ApiRequest<MetaRequest> {

    public MetaRequest() {
        this(null, null);
    }

    public Optional<MetaRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<MetaRequest>> examples() {
        return List.of(new MetaRequest(3L, List.of(12L, 13L)), new MetaRequest(1L, null));
    }
}
