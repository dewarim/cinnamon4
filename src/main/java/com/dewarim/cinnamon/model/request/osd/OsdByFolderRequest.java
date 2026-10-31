package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

@JsonRootName("osdByFolderRequest")
public record OsdByFolderRequest(
        boolean includeSummary,
        // a primitive, so a request that omits it arrives as 0 instead of null
        @Positive long folderId,
        Boolean linksAsOsd,
        boolean includeCustomMetadata,
        VersionPredicate versionPredicate) implements ApiRequest<OsdByFolderRequest> {

    public OsdByFolderRequest {
        if (linksAsOsd == null) {
            linksAsOsd = true;
        }
        if (versionPredicate == null) {
            versionPredicate = VersionPredicate.HEAD;
        }
    }

    public OsdByFolderRequest(long folderId, boolean includeSummary, boolean linksAsOsd, boolean includeCustomMetadata,
                              VersionPredicate versionPredicate) {
        this(includeSummary, folderId, linksAsOsd, includeCustomMetadata, versionPredicate);
    }

    public Optional<OsdByFolderRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<OsdByFolderRequest>> examples() {
        return List.of(new OsdByFolderRequest(6L, false, true, true, VersionPredicate.BRANCH));
    }
}
