package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.DeleteByIdRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "deleteFolderTypeRequest")
public class DeleteFolderTypeRequest extends DeleteByIdRequest<FolderType> implements ApiRequest<DeleteFolderTypeRequest> {

    public DeleteFolderTypeRequest() {
    }

    public DeleteFolderTypeRequest(List<Long> ids) {
        super(ids);
    }

    public DeleteFolderTypeRequest(Long id) {
        super(id);
    }

    @Override
    public List<ApiRequest<DeleteFolderTypeRequest>> examples() {
        return List.of(new DeleteFolderTypeRequest(List.of(543L,44L)));
    }
}
