package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "updateFolderTypeRequest")
public class UpdateFolderTypeRequest implements UpdateRequest<FolderType>, ApiRequest {

    @JacksonXmlElementWrapper(localName = "folderTypes")
    @JacksonXmlProperty(localName = "folderType")
    private List<FolderType> folderTypes = new ArrayList<>();

    @Override
    public List<FolderType> list() {
        return folderTypes;
    }

    public UpdateFolderTypeRequest() {
    }

    public UpdateFolderTypeRequest(Long id, String name) {
        folderTypes.add(new FolderType(id,name));
    }

    public UpdateFolderTypeRequest(List<FolderType> folderTypes) {
        this.folderTypes = folderTypes;
    }

    @Override
    public boolean validated() {
        return folderTypes.stream().allMatch(FolderType ->
            FolderType != null && FolderType.getName() != null && !FolderType.getName().trim().isEmpty()
                    && FolderType.getId() != null && FolderType.getId() > 0);
    }

    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }
}
