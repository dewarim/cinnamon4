package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("folderByRelativePathRequest")
public record FolderByRelativePathRequest(
        @NotEmpty
        @Pattern(regexp = "[^/](.*[^/])?", flags = Pattern.Flag.DOTALL,
                message = "must not start or end with a slash")
        String relativePath,
        @NotNull @Positive Long parentId,
        boolean includeSummary) implements ApiRequest<FolderByRelativePathRequest> {

    public Optional<FolderByRelativePathRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<FolderByRelativePathRequest>> examples() {
        return List.of(new FolderByRelativePathRequest("creation/some-sub-folder", 213L, true));
    }
}
