package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.OwnableWithMetadata;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;

@JacksonXmlRootElement(localName = "folder")
public class Folder implements OwnableWithMetadata, Identifiable {

    private Long    id;
    private String  name;
    private Long    aclId;
    private Long    ownerId;
    private Long    parentId;
    private Long    typeId;
    private boolean metadataChanged;
    private String  summary;
    private String  folderPath;
    /**
     * Is true if other folders have this folder as parent folder.
     * Value is read-only.
     */
    private boolean hasSubfolders = false;

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    private List<Meta> metasets = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created = LocalDateTime.now();

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
        metadataChanged = false;
        created = LocalDateTime.now();
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

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public boolean isHasSubfolders() {
        return hasSubfolders;
    }

    public void setHasSubfolders(boolean hasSubfolders) {
        this.hasSubfolders = hasSubfolders;
    }

    public List<Meta> getMetasets() {
        return metasets;
    }

    public void setMetasets(List<Meta> metasets) {
        this.metasets = metasets;
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
                Objects.equals(aclId, folder.aclId) &&
                Objects.equals(ownerId, folder.ownerId) &&
                Objects.equals(created, folder.created) &&
                Objects.equals(parentId, folder.parentId) &&
                Objects.equals(typeId, folder.typeId) &&
                Objects.equals(metadataChanged, folder.metadataChanged) &&
                Objects.equals(summary, folder.summary);
    }

    @Override
    public boolean isMetadataChanged() {
        return metadataChanged;
    }

    @Override
    public void setMetadataChanged(boolean metadataChanged) {
        this.metadataChanged = metadataChanged;
    }

    @Override
    public void setModified(LocalDateTime modified) {
        // ignore
    }

    @Override
    public void setModifierId(Long modifierId) {
        // ignore
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
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
                ", aclId=" + aclId +
                ", ownerId=" + ownerId +
                ", parentId=" + parentId +
                ", typeId=" + typeId +
                ", metadataChanged=" + metadataChanged +
                ", summary='" + summary + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", hasSubfolders=" + hasSubfolders +
                ", metas=" + metasets +
                ", created=" + created +
                '}';
    }
}
