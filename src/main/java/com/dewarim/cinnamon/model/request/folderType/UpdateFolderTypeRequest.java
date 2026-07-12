package com.dewarim.cinnamon.model.request.folderType;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.FolderTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("updateFolderTypeRequest")
public record UpdateFolderTypeRequest(
        @JacksonXmlElementWrapper(localName = "folderTypes")
        @JacksonXmlProperty(localName = "folderType")
        List<FolderType> folderTypes) implements UpdateRequest<FolderType>, ApiRequest<UpdateFolderTypeRequest> {

    public UpdateFolderTypeRequest {
        if (folderTypes == null) {
            folderTypes = new ArrayList<>();
        }
    }

    public UpdateFolderTypeRequest(Long id, String name) {
        this(new ArrayList<>(List.of(new FolderType(id, name))));
    }

    @Override
    public List<FolderType> list() {
        return folderTypes;
    }

    @Override
    public boolean validated() {
        return folderTypes.stream().allMatch(folderType ->
                folderType != null && folderType.getName() != null && !folderType.getName().trim().isEmpty()
                        && folderType.getId() != null && folderType.getId() > 0);
    }

    @Override
    public Wrapper<FolderType> fetchResponseWrapper() {
        return new FolderTypeWrapper();
    }

    @Override
    public List<ApiRequest<UpdateFolderTypeRequest>> examples() {
        return List.of(new UpdateFolderTypeRequest(665L, "almost-evil-type"));
    }
}
