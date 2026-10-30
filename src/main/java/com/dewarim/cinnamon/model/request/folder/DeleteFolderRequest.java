package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.Folder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.*;

@JsonRootName("deleteFolderRequest")
public record DeleteFolderRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        @NotEmpty Set<@NotNull @Positive Long> ids,
        boolean deleteRecursively,
        boolean deleteContent) implements ApiRequest<Folder> {

    public DeleteFolderRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteFolderRequest(List<Long> ids, boolean deleteRecursively, boolean deleteContent) {
        this(new HashSet<>(ids), deleteRecursively, deleteContent);
    }

    public boolean isDeleteRecursively() {
        return deleteRecursively;
    }

    public boolean isDeleteContent() {
        return deleteContent;
    }

    @Override
    public List<ApiRequest<Folder>> examples() {
        return List.of(new DeleteFolderRequest(List.of(1L, 2L, 3L), true, false));
    }

    public Optional<DeleteFolderRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }
}
