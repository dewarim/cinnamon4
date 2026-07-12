package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("createFolderTypeRequest")
public record CreateFolderTypeRequest(
        @JacksonXmlElementWrapper(localName = "folderTypes")
        @JacksonXmlProperty(localName = "folderType")
        List<FolderType> folderTypes) implements CreateRequest<FolderType>, ApiRequest<CreateFolderTypeRequest> {

    public CreateFolderTypeRequest {
        if (folderTypes == null) {
            folderTypes = new ArrayList<>();
        }
    }

    public CreateFolderTypeRequest(String name) {
        this(new ArrayList<>(List.of(new FolderType(name))));
    }

    @Override
    public List<FolderType> list() {
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
