package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "createFolderTypeRequest")
public class CreateFolderTypeRequest implements CreateRequest<FolderType>, ApiRequest<CreateFolderTypeRequest> {

    @JacksonXmlElementWrapper(localName = "folderTypes")
    @JacksonXmlProperty(localName = "folderType")
    private List<FolderType> folderTypes = new ArrayList<>();

    @Override
    public List<FolderType> list() {
        return folderTypes;
    }

    public CreateFolderTypeRequest() {
    }

    public CreateFolderTypeRequest(List<FolderType> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public CreateFolderTypeRequest(String name) {
        this.folderTypes.add(new FolderType(name));
    }

    public List<FolderType> getFolderTypes() {
        return folderTypes;
    }

    @Override
    public boolean validated() {
        return folderTypes.stream().noneMatch(folderType -> folderType == null ||
                folderType.getName() == null || folderType.getName().trim().isEmpty());
    }

    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }

    @Override
    public List<ApiRequest<CreateFolderTypeRequest>> examples() {
        return List.of(new CreateFolderTypeRequest("source"), new CreateFolderTypeRequest(List.of(
                new FolderType("temp"), new FolderType("bin")
        )));
    }
}
