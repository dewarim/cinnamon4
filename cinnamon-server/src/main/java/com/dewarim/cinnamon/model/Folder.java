package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Accessible;
import com.dewarim.cinnamon.api.Ownable;

import java.util.Date;
import java.util.Objects;

public class Folder implements Ownable, Accessible {
    
    private Long id;
    private String name;
    private Long objVersion;
    private Long aclId;
    private Long ownerId;
    private Long parentId;
    private Long typeId;
    private Boolean metadataChanged;
    private String summary;
    private Date created;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
}
