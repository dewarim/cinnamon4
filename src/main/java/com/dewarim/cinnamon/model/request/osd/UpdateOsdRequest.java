package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "updateOsdRequest")
public class UpdateOsdRequest implements ApiRequest {

    private Long   id;
    private Long   parentFolderId;
    private String name;
    private Long   ownerId;
    private Long   aclId;
    private Long   objectTypeId;
    private Long   languageId;
    private Boolean metadataChanged;
    private Boolean contentChanged;

    public UpdateOsdRequest() {
    }

    public UpdateOsdRequest(Long id, Long parentFolderId, String name, Long ownerId, Long aclId, Long objectTypeId, Long languageId) {
        this.id = id;
        this.parentFolderId = parentFolderId;
        this.name = name;
        this.ownerId = ownerId;
        this.aclId = aclId;
        this.objectTypeId = objectTypeId;
        this.languageId = languageId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
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

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public Long getObjectTypeId() {
        return objectTypeId;
    }

    public void setObjectTypeId(Long objectTypeId) {
        this.objectTypeId = objectTypeId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public Boolean getMetadataChanged() {
        return metadataChanged;
    }

    public void setMetadataChanged(Boolean metadataChanged) {
        this.metadataChanged = metadataChanged;
    }

    public Boolean getContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(Boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    @Override
    public String toString() {
        return "UpdateOsdRequest{" +
                "id=" + id +
                ", parentFolderId=" + parentFolderId +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", aclId=" + aclId +
                ", objectTypeId=" + objectTypeId +
                ", languageId=" + languageId +
                ", metadataChanged=" + metadataChanged +
                ", contentChanged=" + contentChanged +
                '}';
    }

    /**
     * A valid UpdateOsdRequest must contain the object's id as well at least
     * one potentially valid field that should be updated.
     */
    private boolean validated() {
        if (name != null && (name.length() == 0 || name.trim().length() < name.length() || name.matches("^\\s+$"))) {
            return false;
        }
        if (parentFolderId != null && parentFolderId <= 0) {
            return false;
        }
        if (ownerId != null && ownerId <= 0) {
            return false;
        }
        if (aclId != null && aclId <= 0) {
            return false;
        }
        if (objectTypeId != null && objectTypeId <= 0) {
            return false;
        }
        if (languageId != null && languageId <= 0) {
            return false;
        }
        // an update request that does not change anything is invalid.
        if (name == null && parentFolderId == null && ownerId == null && aclId == null && objectTypeId == null && languageId == null) {
            return false;
        }
        return id != null && id > 0;
    }

    public Optional<UpdateOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest> examples() {
        UpdateOsdRequest request = new UpdateOsdRequest(1L, 2L, "new name", 45L, 56L, 1L, 1L);
        request.setContentChanged(true);
        request.setMetadataChanged(false);
        return List.of(request);
    }
}
