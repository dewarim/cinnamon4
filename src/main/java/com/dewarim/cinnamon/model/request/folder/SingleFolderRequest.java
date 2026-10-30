package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

/**
 * Request for a single folder and its ancestors.
 */
@JsonRootName("singleFolderRequest")
public record SingleFolderRequest(@NotNull @Positive Long id, boolean includeSummary) implements ApiRequest<Folder> {

    public SingleFolderRequest() {
        this(null, false);
    }

    public Optional<SingleFolderRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<Folder>> examples() {
        return List.of(new SingleFolderRequest(123L, true), new SingleFolderRequest(321L, false));
    }
}
