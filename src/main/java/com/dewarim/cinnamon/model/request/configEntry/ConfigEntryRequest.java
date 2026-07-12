package com.dewarim.cinnamon.model.request.configEntry;

import com.dewarim.cinnamon.api.ApiRequest;
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
        List<Long> ids) implements ApiRequest<ConfigEntryRequest> {

    public ConfigEntryRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public ConfigEntryRequest(Long id) {
        this(new ArrayList<>(java.util.Collections.singletonList(id)));
    }

    public boolean validated() {
        return (ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id != null && id > 0));
    }

    public Optional<ConfigEntryRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<ConfigEntryRequest>> examples() {
        return List.of(new ConfigEntryRequest(123L), new ConfigEntryRequest(456L));
    }
}
