package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "listFolderTypeRequest")
public class ListFolderTypeRequest extends DefaultListRequest implements ListRequest<FolderType>, ApiRequest<ListFolderTypeRequest> {
    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }

    @Override
    public List<ApiRequest<ListFolderTypeRequest>> examples() {
        return List.of(new ListFolderTypeRequest());
    }
}
