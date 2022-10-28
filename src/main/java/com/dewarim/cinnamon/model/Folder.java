package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.Ownable;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.Objects;

import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;

public class Folder implements Ownable, Identifiable {

    private Long    id;
    private String  name;
    private Long    objVersion;
    private Long    aclId;
    private Long    ownerId;
    private Long    parentId;
    private Long    typeId;
    private Boolean metadataChanged;
    private String  summary;
    /**
     * Is true if other folders have this folder as parent folder.
     * Value is read-only.
     */
    private boolean hasSubfolders = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date created = new Date();

    public Folder() {
        summary = DEFAULT_SUMMARY;
    }

    public Folder(String name, Long aclId, Long ownerId, Long parentId, Long typeId, String summary) {
        this.name = name;
        this.aclId = aclId;
        this.ownerId = ownerId;
        this.parentId = parentId;
        this.typeId = typeId;
        this.summary = Objects.requireNonNullElse(summary, DEFAULT_SUMMARY);
        objVersion = 0L;
        metadataChanged = false;
        created = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getObjVersion() {
        return objVersion;
    }

    public void setObjVersion(Long objVersion) {
        this.objVersion = objVersion;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
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

    public Boolean getMetadataChanged() {
        return metadataChanged;
    }

    public void setMetadataChanged(Boolean metadataChanged) {
        this.metadataChanged = metadataChanged;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isHasSubfolders() {
        return hasSubfolders;
    }

    public void setHasSubfolders(boolean hasSubfolders) {
        this.hasSubfolders = hasSubfolders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Folder folder = (Folder) o;
        return Objects.equals(name, folder.name) &&
                Objects.equals(objVersion, folder.objVersion) &&
                Objects.equals(aclId, folder.aclId) &&
                Objects.equals(ownerId, folder.ownerId) &&
                Objects.equals(created, folder.created) &&
                Objects.equals(parentId, folder.parentId) &&
                Objects.equals(typeId, folder.typeId) &&
                Objects.equals(metadataChanged, folder.metadataChanged) &&
                Objects.equals(summary, folder.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parentId);
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", objVersion=" + objVersion +
                ", aclId=" + aclId +
                ", ownerId=" + ownerId +
                ", parentId=" + parentId +
                ", typeId=" + typeId +
                ", metadataChanged=" + metadataChanged +
                ", summary='" + summary + '\'' +
                ", created=" + created +
                ", subfolders=" + hasSubfolders +
                '}';
    }
}
