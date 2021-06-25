package com.dewarim.cinnamon.model.request.folder;

import java.util.Optional;

public class CreateFolderRequest {

    private String name;
    private Long   parentId;
    private String summary = "<summary />";
    private Long   ownerId;
    private Long   aclId;
    private Long   typeId;

    public CreateFolderRequest() {
    }

    public CreateFolderRequest(String name, Long parentId, String summary, Long ownerId, Long aclId, Long typeId) {
        this.name = name;
        this.parentId = parentId;
        this.summary = summary;
        this.ownerId = ownerId;
        this.aclId = aclId;
        this.typeId = typeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    private boolean validated() {
        return name != null && name.trim().length() > 0
                && parentId != null && parentId > 0
                && (typeId == null || typeId > 0)
                && (aclId == null || aclId > 0)
                && (ownerId == null || ownerId > 0)
                && summary != null
                && summary.trim().length() > 0;
    }

    public Optional<CreateFolderRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "CreateFolderRequest{" +
                "name='" + name + '\'' +
                ", parentId=" + parentId +
                ", summary='" + summary + '\'' +
                ", ownerId=" + ownerId +
                ", aclId=" + aclId +
                ", typeId=" + typeId +
                '}';
    }
}
