package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
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
        List<Long> ids,
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

    /**
     * @return true if list of ids is non-empty and contains only positive long integers.
     */
    private boolean validated() {
        return ids != null && !ids.isEmpty() && ids.stream().allMatch(id -> id != null && id > 0);
    }

    public Optional<FolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<FolderRequest>> examples() {
        return List.of(new FolderRequest(List.of(1L, 2L, 3L), true, true));
    }
}
