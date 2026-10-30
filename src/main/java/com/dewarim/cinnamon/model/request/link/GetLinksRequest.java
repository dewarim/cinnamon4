package com.dewarim.cinnamon.model.request.link;

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

@JsonRootName("getLinksRequest")
public record GetLinksRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<@NotNull @Positive Long> ids,
        boolean includeSummary) implements ApiRequest<GetLinksRequest> {

    public GetLinksRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public GetLinksRequest(Long id, boolean includeSummary) {
        this(new ArrayList<>(java.util.Collections.singletonList(id)), includeSummary);
    }

    public Optional<GetLinksRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<GetLinksRequest>> examples() {
        return List.of(new GetLinksRequest(9L, true));
    }
}
