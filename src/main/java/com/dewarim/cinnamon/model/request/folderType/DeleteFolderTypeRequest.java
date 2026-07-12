package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonRootName("deleteFolderTypeRequest")
public record DeleteFolderTypeRequest(
        @JacksonXmlElementWrapper(localName = "ids")
        @JacksonXmlProperty(localName = "id")
        Set<Long> ids,
        boolean ignoreNotFound) implements DeleteByIdRequest<FolderType>, ApiRequest<DeleteFolderTypeRequest> {

    public DeleteFolderTypeRequest {
        if (ids == null) {
            ids = new HashSet<>();
        }
    }

    public DeleteFolderTypeRequest(List<Long> ids) {
        this(new HashSet<>(ids), false);
    }

    public DeleteFolderTypeRequest(Long id) {
        this(new HashSet<>(java.util.Collections.singletonList(id)), false);
    }

    @Override
    public List<ApiRequest<DeleteFolderTypeRequest>> examples() {
        return List.of(new DeleteFolderTypeRequest(List.of(543L, 44L)));
    }
}
