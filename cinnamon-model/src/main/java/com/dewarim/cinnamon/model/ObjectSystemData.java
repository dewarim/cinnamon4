package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.provider.DefaultContentProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Core Cinnamon object, contains the system's data of an object (document, media file, other resource).
 * It's abbreviated to OSD.
 */
public class ObjectSystemData implements ContentMetadata, CinnamonObject {

    private Long    id;
    private String  name;
    private String  contentPath;
    private Long    contentSize;
    private Long    predecessorId;
    private Long    rootId;
    private Long    creatorId;
    private Long    modifierId;
    private Long    ownerId;
    private Long    lockerId;
    private Date    created         = new Date();
    private Date    modified        = new Date();
    private Long    languageId;
    private Long    aclId;
    private Long    parentId;
    private Long    formatId;
    private Long    typeId;

    /**
     * An object is latestHead, if it is not of part of a branch and has no
     * descendants.
     */
    private boolean latestHead;

     /**
     * An object is latestBranch, if it has no descendants and its cmnVersion contains a ".".
     * You can only delete an object without descendants.
     */
    private boolean latestBranch    = true;
    private boolean contentChanged  = false;
    private boolean metadataChanged = false;
    private String  cmnVersion      = "1";
    private Long    lifecycleStateId;
    private String  summary         = "<summary/>";

    private Long   objVersion;
    private String contentHash;
    private String contentProvider = DefaultContentProvider.FILE_SYSTEM.name();

    public ObjectSystemData() {
    }

    /**
     * Create a new version of the current object.
     * <br/>
     * Does not copy lifecycle state or content.
     *
     * @param user
     * @return
     */
    public ObjectSystemData createNewVersion(UserAccount user, String lastDescendantVersion) {
        ObjectSystemData nextVersion = new ObjectSystemData();
        Calendar         calendar    = Calendar.getInstance();
        nextVersion.setAclId(aclId);
        nextVersion.setCreated(calendar.getTime());
        nextVersion.setCreatorId(user.getId());
        nextVersion.setModified(calendar.getTime());
        nextVersion.setModifierId(user.getId());
        nextVersion.setOwnerId(user.getId());
        nextVersion.setLanguageId(languageId);
        nextVersion.setLatestHead(latestHead);
        nextVersion.setLatestBranch(latestBranch);
        nextVersion.setLockerId(null);
        nextVersion.setName(name);
        nextVersion.setParentId(parentId);
        nextVersion.setRootId(rootId);
        nextVersion.setPredecessorId(id);
        nextVersion.setTypeId(typeId);
        String nextVersionLabel = createNewVersionLabel(lastDescendantVersion);
        nextVersion.setCmnVersion(nextVersionLabel);
        return nextVersion;
    }

    /**
     * Create a new version label for this object. This method should only be used
     * by OSD.createNewVersion
     *
     * @param lastDescendantVersion
     * @return new version label
     */
    private String createNewVersionLabel(String lastDescendantVersion) {
        String   predecessorVersion = cmnVersion;
        String[] branches           = predecessorVersion.split("\\.");
        String   lastSegment        = branches[branches.length - 1];
        String[] lastBranch         = lastSegment.split("-");

        if (lastDescendantVersion == null) {
            // no object with same predecessor
            String buffer = lastBranch.length == 2 ? lastBranch[1] : lastBranch[0];
            String stem   = predecessorVersion.substring(0, predecessorVersion.length() - buffer.length());
            buffer = String.valueOf(Integer.parseInt(buffer) + 1);
            return stem + buffer;
        }
        String[] lastDescBranches = lastDescendantVersion.split("\\.");
        if (branches.length == lastDescBranches.length) {
            // last descendant is the only one so far: create first branch
            return predecessorVersion + ".1-1";
        }
        String buffer = lastDescBranches[lastDescBranches.length - 1].split("-")[0];
        buffer = String.valueOf(Integer.parseInt(buffer) + 1);
        return predecessorVersion + "." + buffer + "-1";
    }

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

    public void setAclId(long aclId) {
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

    /**
     * @return Name of content provider. Default: FILE_SYSTEM
     */
    public String getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(String contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObjectSystemData that = (ObjectSystemData) o;
        return latestHead == that.latestHead &&
                latestBranch == that.latestBranch &&
                Objects.equals(name, that.name) &&
                Objects.equals(contentSize, that.contentSize) &&
                Objects.equals(predecessorId, that.predecessorId) &&
                Objects.equals(rootId, that.rootId) &&
                Objects.equals(creatorId, that.creatorId) &&
                Objects.equals(modifierId, that.modifierId) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(lockerId, that.lockerId) &&
                Objects.equals(created, that.created) &&
                Objects.equals(modified, that.modified) &&
                Objects.equals(languageId, that.languageId) &&
                Objects.equals(aclId, that.aclId) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(formatId, that.formatId) &&
                Objects.equals(typeId, that.typeId) &&
                Objects.equals(cmnVersion, that.cmnVersion) &&
                Objects.equals(lifecycleStateId, that.lifecycleStateId) &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(objVersion, that.objVersion) &&
                Objects.equals(contentHash, that.contentHash) &&
                Objects.equals(contentProvider, that.contentProvider);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, ownerId, created, parentId, cmnVersion);
    }
}
