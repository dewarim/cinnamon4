package com.dewarim.cinnamon.model.request.folder;

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

@JsonRootName("folderRequest")
public record FolderRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty List<@NotNull @Positive Long> ids,
        boolean includeSummary,
        boolean addFolderPath) implements ApiRequest<FolderRequest> {

    public FolderRequest {
        if (ids == null) {
            ids = new ArrayList<>();
        }
    }

    public FolderRequest(List<Long> ids, boolean includeSummary) {
        this(ids, includeSummary, false);
    }

    public Optional<FolderRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<FolderRequest>> examples() {
        return List.of(new FolderRequest(List.of(1L, 2L, 3L), true, true));
    }
}
