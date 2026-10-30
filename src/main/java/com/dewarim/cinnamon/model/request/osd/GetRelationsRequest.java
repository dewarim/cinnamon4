package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("getRelationsRequest")
public record GetRelationsRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<Long> ids,
        boolean includeMetadata) implements ApiRequest<GetRelationsRequest> {

    public GetRelationsRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public Optional<GetRelationsRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<GetRelationsRequest>> examples() {
        return List.of(new GetRelationsRequest(List.of(1L, 32L, 4L), false));
    }
}
