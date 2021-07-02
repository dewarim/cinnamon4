package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;

import java.util.List;

public class DeleteFolderTypeRequest extends DeleteByIdRequest<FolderType> implements ApiRequest {

    public DeleteFolderTypeRequest() {
    }

    public DeleteFolderTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteFolderTypeRequest(Long id) {
        super(id);
    }
}
