package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Folder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "updateFolderRequest")
public class UpdateFolderRequest implements ApiRequest<UpdateFolderRequest> {

    @JacksonXmlElementWrapper(localName = "folders")
    @JacksonXmlProperty(localName = "folder")
    List<Folder> folders = new ArrayList<>();

    public UpdateFolderRequest() {
    }

    public UpdateFolderRequest(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId, Boolean metadataChanged) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setParentId(parentId);
        folder.setName(name);
        folder.setOwnerId(ownerId);
        folder.setTypeId(typeId);
        folder.setAclId(aclId);
        folder.setMetadataChanged(metadataChanged);
        folders.add(folder);
    }

    public UpdateFolderRequest(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId) {
        this(id, parentId, name, ownerId, typeId, aclId, null);
    }

    public UpdateFolderRequest(List<Folder> folder) {
        this.folders.addAll(folder);
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    private boolean validated() {
        if (folders == null || folders.isEmpty()) {
            return false;
        }
        return folders.stream().allMatch(folder ->
                folder.getId() != null && folder.getId() > 0
                        && (folder.getParentId() == null || folder.getParentId() > 0)
                        && (folder.getName() == null || !folder.getName().trim().isEmpty())
                        && (folder.getTypeId() == null || folder.getTypeId() > 0)
                        && (folder.getOwnerId() == null || folder.getOwnerId() > 0)
                        && (folder.getAclId() == null || folder.getAclId() > 0)
        );
    }

    public Optional<UpdateFolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "UpdateFolderRequest{" +
                "folders=" + folders +
                '}';
    }

    @Override
    public List<ApiRequest<UpdateFolderRequest>> examples() {
        Folder folder = new Folder("new name", 1L, 2L, 3L, 4L, "<summary>update this</summary>");
        folder.setMetadataChanged(true);
        UpdateFolderRequest request = new UpdateFolderRequest(List.of(folder));
        return List.of(request);
    }
}
