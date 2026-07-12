package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListType;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;

@JsonRootName("listFolderTypeRequest")
public record ListFolderTypeRequest(ListType type) implements DefaultListRequest, ListRequest<FolderType>, ApiRequest<ListFolderTypeRequest> {

    public ListFolderTypeRequest {
        if (type == null) {
            type = ListType.FULL;
        }
    }

    public ListFolderTypeRequest() {
        this(ListType.FULL);
    }
    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListFolderTypeRequest>> examples() {
        return List.of(new ListFolderTypeRequest());
    }
}
