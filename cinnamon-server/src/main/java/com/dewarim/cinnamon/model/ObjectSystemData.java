package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.provider.DefaultContentProvider;

import java.util.Date;

/**
 * Core Cinnamon object, contains the system's data of an object (document, media file, other resource).
 * It's abbreviated to OSD.
 */
public class ObjectSystemData implements ContentMetadata {
    
    private Long id;
    private String name;
    private String applicationName = "";
    
    private String contentPath;
    private Long contentSize;
    private Long predecessorId;
    private Long rootId;
    private Long creatorId;
    private Long modifierId;
    private Long ownerId;
    private Long lockerId;
    private Date created = new Date();
    private Date modified = new Date();
    private Long languageId;
    private Long aclId;
    private Long parentId;
    private Long formatId;
    private Long typeId;
    private boolean latestHead;
    private boolean latestBranch = true;
    private boolean contentChanged = false;
    private boolean metadataChanged = false;
    private String cmnVersion = "1";
    private Long lifecycleStateId;
    private String summary = "<summary/>";
    
    private Long objVersion;
    private String contentHash;
    private String contentProvider = DefaultContentProvider.FILE_SYSTEM.name();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public Long getContentSize() {
        return contentSize;
    }

    public void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    public Long getPredecessorId() {
        return predecessorId;
    }

    public void setPredecessorId(Long predecessorId) {
        this.predecessorId = predecessorId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getModifierId() {
        return modifierId;
    }

    public void setModifierId(Long modifierId) {
        this.modifierId = modifierId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getLockerId() {
        return lockerId;
    }

    public void setLockerId(Long lockerId) {
        this.lockerId = lockerId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getFormatId() {
        return formatId;
    }

    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public boolean isLatestHead() {
        return latestHead;
    }

    public void setLatestHead(boolean latestHead) {
        this.latestHead = latestHead;
    }

    public Long getObjVersion() {
        return objVersion;
    }

    public void setObjVersion(Long objVersion) {
        this.objVersion = objVersion;
    }

    public boolean isLatestBranch() {
        return latestBranch;
    }

    public void setLatestBranch(boolean latestBranch) {
        this.latestBranch = latestBranch;
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public boolean isMetadataChanged() {
        return metadataChanged;
    }

    public void setMetadataChanged(boolean metadataChanged) {
        this.metadataChanged = metadataChanged;
    }

    public String getCmnVersion() {
        return cmnVersion;
    }

    public void setCmnVersion(String cmnVersion) {
        this.cmnVersion = cmnVersion;
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

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(String contentProvider) {
        this.contentProvider = contentProvider;
    }
}
