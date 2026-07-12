package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("folderPathRequest")
public record FolderPathRequest(String path, boolean includeSummary) implements ApiRequest<FolderPathRequest> {

    public boolean validated() {
        return path != null && !path.isEmpty();
    }

    @Override
    public List<ApiRequest<FolderPathRequest>> examples() {
        return List.of(new FolderPathRequest("/home/creation/some-sub-folder", true));
    }
}
