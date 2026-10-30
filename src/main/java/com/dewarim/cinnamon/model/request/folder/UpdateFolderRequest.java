package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("updateFolderRequest")
public record UpdateFolderRequest(
        @JacksonXmlElementWrapper(localName = "folders")
        @JacksonXmlProperty(localName = "folder")
        @NotEmpty List<@Valid Folder> folders,
        boolean updateMetadataChanged) implements ApiRequest<UpdateFolderRequest> {

    public UpdateFolderRequest {
        if (folders == null) {
            folders = new ArrayList<>();
        }
    }

    public UpdateFolderRequest() {
        this(new ArrayList<>(), false);
    }

    public UpdateFolderRequest(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId, Boolean metadataChanged) {
        this(singleFolder(id, parentId, name, ownerId, typeId, aclId, metadataChanged), false);
    }

    public UpdateFolderRequest(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId) {
        this(id, parentId, name, ownerId, typeId, aclId, true);
    }

    public UpdateFolderRequest(List<Folder> folder) {
        this(new ArrayList<>(folder), false);
    }

    private static List<Folder> singleFolder(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId, Boolean metadataChanged) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setParentId(parentId);
        folder.setName(name);
        folder.setOwnerId(ownerId);
        folder.setTypeId(typeId);
        folder.setAclId(aclId);
        folder.setMetadataChanged(metadataChanged);
        List<Folder> list = new ArrayList<>();
        list.add(folder);
        return list;
    }

    public boolean isUpdateMetadataChanged() {
        return updateMetadataChanged;
    }

    public Optional<UpdateFolderRequest> validateRequest() {
        RequestValidator.validate(this, UpdateRequest.class);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<UpdateFolderRequest>> examples() {
        Folder folder = new Folder("new name", 1L, 2L, 3L, 4L, "<summary>update this</summary>");
        folder.setCreated(LOCAL_DATE_TIME_EXAMPLE);
        return List.of(new UpdateFolderRequest(List.of(folder)));
    }
}
