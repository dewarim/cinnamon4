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

@JsonRootName("osdRequest")
public record OsdRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<@NotNull @Positive Long> ids,
        boolean includeSummary,
        boolean includeCustomMetadata,
        boolean addFolderPath) implements ApiRequest<OsdRequest> {

    public OsdRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public OsdRequest(List<Long> ids, boolean includeSummary, boolean includeCustomMetadata) {
        this(ids, includeSummary, includeCustomMetadata, false);
    }

    public Optional<OsdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<OsdRequest>> examples() {
        return List.of(new OsdRequest(List.of(45L, 23L, 2L), true, true, true));
    }
}
