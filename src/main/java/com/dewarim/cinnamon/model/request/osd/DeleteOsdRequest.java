package com.dewarim.cinnamon.model.request.osd;

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

@JsonRootName("deleteOsdRequest")
public record DeleteOsdRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<@NotNull @Positive Long> ids,
        boolean deleteDescendants,
        boolean deleteAllVersions) implements ApiRequest<DeleteOsdRequest> {

    public DeleteOsdRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public Optional<DeleteOsdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<DeleteOsdRequest>> examples() {
        return List.of(new DeleteOsdRequest(List.of(4L, 6L), true, true));
    }
}
