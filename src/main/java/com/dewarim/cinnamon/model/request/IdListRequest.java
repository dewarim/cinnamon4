package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JsonRootName("idListRequest")
public record IdListRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids) implements ApiRequest<IdListRequest> {

    public IdListRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public IdListRequest(List<Long> ids) {
        this(new HashSet<>(ids));
    }

    public boolean validated() {
        if (ids == null) {
            return false;
        }
        return ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<IdListRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<IdListRequest>> examples() {
        return List.of(new IdListRequest(List.of(1L, 44L, 5L)));
    }
}
