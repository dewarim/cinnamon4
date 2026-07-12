package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("folderByRelativePathRequest")
public record FolderByRelativePathRequest(String relativePath, Long parentId, boolean includeSummary) implements ApiRequest<FolderByRelativePathRequest> {

    public boolean validated() {
        return relativePath != null &&
                !relativePath.isEmpty() &&
                !(relativePath.startsWith("/") || relativePath.endsWith("/")) &&
                parentId != null && parentId > 0;
    }

    @Override
    public List<ApiRequest<FolderByRelativePathRequest>> examples() {
        return List.of(new FolderByRelativePathRequest("creation/some-sub-folder", 213L, true));
    }
}
