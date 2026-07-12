package com.dewarim.cinnamon.model.request.link;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("getLinksRequest")
public record GetLinksRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        List<Long> ids,
        boolean includeSummary) implements ApiRequest<GetLinksRequest> {

    public GetLinksRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public GetLinksRequest(Long id, boolean includeSummary) {
        this(new ArrayList<>(java.util.Collections.singletonList(id)), includeSummary);
    }

    public boolean validated() {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<GetLinksRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<GetLinksRequest>> examples() {
        return List.of(new GetLinksRequest(9L, true));
    }
}
