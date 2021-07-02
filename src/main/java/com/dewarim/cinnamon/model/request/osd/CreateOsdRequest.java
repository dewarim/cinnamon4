package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;

import java.util.Optional;

public class CreateOsdRequest implements ApiRequest {

    private String name;
    private Long   parentId;
    private Long   ownerId;
    private Long   aclId;
    private Long   typeId;
    private Long   formatId;
    private Long   languageId;
    private Long   lifecycleStateId;
    private String summary = "<summary />";

    public CreateOsdRequest() {
    }

    public CreateOsdRequest(String name, Long parentId, Long ownerId, Long aclId,
                            Long typeId, Long formatId, Long languageId,
                            Long lifecycleStateId, String summary) {
        this.name = name;
        this.parentId = parentId;
        this.ownerId = ownerId;
        this.aclId = aclId;
        this.typeId = typeId;
        this.formatId = formatId;
        this.languageId = languageId;
        this.lifecycleStateId = lifecycleStateId;
        this.summary = summary;
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

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public Long getLifecycleStateId() {
        return lifecycleStateId;
    }

    public void setLifecycleStateId(Long lifecycleStateId) {
        this.lifecycleStateId = lifecycleStateId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    private boolean validated() {
        return name != null && name.trim().length() > 0
                && parentId != null && parentId > 0
                && (typeId == null || typeId > 0)
                && (aclId == null || aclId > 0)
                && (ownerId == null || ownerId > 0)
                && (formatId == null ||  formatId > 0)
                && (languageId == null || languageId > 0)
                && summary != null
                && summary.trim().length() > 0;
    }

    public Optional<CreateOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "CreateOsdRequest{" +
                "name='" + name + '\'' +
                ", parentId=" + parentId +
                ", ownerId=" + ownerId +
                ", aclId=" + aclId +
                ", typeId=" + typeId +
                ", formatId=" + formatId +
                ", languageId=" + languageId +
                ", lifecycleStateId=" + lifecycleStateId +
                ", summary='" + summary + '\'' +
                '}';
    }
}
