package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("osdByFolderRequest")
public record OsdByFolderRequest(
        boolean includeSummary,
        long folderId,
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

    @Override
    public List<ApiRequest<OsdByFolderRequest>> examples() {
        return List.of(new OsdByFolderRequest(6L, false, true, true, VersionPredicate.BRANCH));
    }
}
