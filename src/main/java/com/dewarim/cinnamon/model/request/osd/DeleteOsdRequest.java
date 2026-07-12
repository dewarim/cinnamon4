package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonRootName("deleteOsdRequest")
public record DeleteOsdRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        List<Long> ids,
        boolean deleteDescendants,
        boolean deleteAllVersions) implements ApiRequest<DeleteOsdRequest> {

    public DeleteOsdRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    private boolean validated() {
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> Objects.nonNull(id) && id > 0);
    }

    public Optional<DeleteOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public List<ApiRequest<DeleteOsdRequest>> examples() {
        return List.of(new DeleteOsdRequest(List.of(4L, 6L), true, true));
    }
}
