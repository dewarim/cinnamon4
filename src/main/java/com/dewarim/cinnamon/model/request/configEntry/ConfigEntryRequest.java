package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Request a list of config entries either by a list of ids or names
 */
@JsonRootName("configEntryRequest")
public record ConfigEntryRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<@NotNull @Positive Long> ids) implements ApiRequest<ConfigEntryRequest> {

    public ConfigEntryRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public ConfigEntryRequest(Long id) {
        this(new ArrayList<>(java.util.Collections.singletonList(id)));
    }

    public Optional<ConfigEntryRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ConfigEntryRequest>> examples() {
        return List.of(new ConfigEntryRequest(123L), new ConfigEntryRequest(456L));
    }
}
