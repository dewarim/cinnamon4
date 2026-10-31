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

@JsonRootName("copyOsdRequest")
public record CopyOsdRequest(
        @JacksonXmlElementWrapper(localName = "sourceIds")
        @JacksonXmlProperty(localName = "sourceId")
        @NotEmpty List<@NotNull @Positive Long> sourceIds,
        @NotNull @Positive Long targetFolderId,
        @JacksonXmlElementWrapper(localName = "metasetTypeIds")
        @JacksonXmlProperty(localName = "metasetTypeId")
        List<@NotNull @Positive Long> metasetTypeIds) implements ApiRequest<CopyOsdRequest> {

    public CopyOsdRequest {
        if (sourceIds == null) {
            sourceIds = new ArrayList<>();
        }
        if (metasetTypeIds == null) {
            metasetTypeIds = new ArrayList<>();
        }
    }

    public Optional<CopyOsdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<CopyOsdRequest>> examples() {
        return List.of(new CopyOsdRequest(List.of(1L, 2L, 3L), 20L, List.of(13L, 15L, 2L)));
    }
}
