package com.dewarim.cinnamon.model.request.folder;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Optional;

@JacksonXmlRootElement(localName = "updateFolderRequest")
public class UpdateFolderRequest implements ApiRequest {

    private Long   id;
    private Long   parentId;
    private String name;
    private Long   ownerId;
    private Long   typeId;
    private Long   aclId;

    public UpdateFolderRequest() {
    }

    public UpdateFolderRequest(Long id, Long parentId, String name, Long ownerId, Long typeId, Long aclId) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.ownerId = ownerId;
        this.typeId = typeId;
        this.aclId = aclId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    private boolean validated() {
        return id != null && id > 0
                && (parentId == null || parentId > 0)
                && (name == null || !name.trim().isEmpty())
                && (typeId == null || typeId > 0)
                && (ownerId == null || ownerId > 0)
                && (aclId == null || aclId > 0);
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
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", typeId=" + typeId +
                ", aclId=" + aclId +
                '}';
    }
}
