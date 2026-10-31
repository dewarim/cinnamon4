package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Optional;

@JsonRootName("folderPathRequest")
public record FolderPathRequest(@NotEmpty String path, boolean includeSummary) implements ApiRequest<FolderPathRequest> {

    public Optional<FolderPathRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<FolderPathRequest>> examples() {
        return List.of(new FolderPathRequest("/home/creation/some-sub-folder", true));
    }
}
