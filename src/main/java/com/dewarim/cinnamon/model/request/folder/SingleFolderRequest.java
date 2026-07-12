package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

/**
 * Request for a single folder and its ancestors.
 */
@JsonRootName("singleFolderRequest")
public record SingleFolderRequest(Long id, boolean includeSummary) implements ApiRequest<Folder> {

    public SingleFolderRequest() {
        this(null, false);
    }

    /**
     * @return true if the id is a positive long integer.
     */
    private boolean validated() {
        return id != null && id > 0;
    }

    public Optional<SingleFolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<Folder>> examples() {
        return List.of(new SingleFolderRequest(123L, true), new SingleFolderRequest(321L, false));
    }
}
