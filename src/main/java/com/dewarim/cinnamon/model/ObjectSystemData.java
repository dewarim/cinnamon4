package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.provider.DefaultContentProvider;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Core Cinnamon object, contains the system's data of an object (document, media file, other resource).
 * It's abbreviated to OSD.
 */
public class ObjectSystemData implements ContentMetadata, CinnamonObject, Identifiable {

    private Long   id;
    private String name;
    private String contentPath;
    private Long   contentSize;
    private Long   predecessorId;
    private Long   rootId;
    private Long   creatorId;
    private Long   modifierId;
    private Long   ownerId;
    private Long   lockerId;
    private Date   created  = new Date();
    private Date   modified = new Date();
    private Long   languageId;
    private Long   aclId;
    private Long   parentId;
    private Long   formatId;
    private Long   typeId;

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "meta")
    private List<Meta> metas;

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

    private Long   objVersion      = 0L;
    private String contentHash;
    private String contentProvider = DefaultContentProvider.FILE_SYSTEM.name();

    public ObjectSystemData() {
    }

    /**
     * Create a new version of the current object.
     * <br/>
     * Does not copy lifecycle state or content.
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
        nextVersion.setLatestBranch(true);
        nextVersion.setLockerId(null);
        nextVersion.setName(name);
        nextVersion.setParentId(parentId);
        nextVersion.setRootId(Objects.requireNonNullElse(rootId, this.getId()));
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
     * @param lastDescendantVersion latest object (highest id) with the same predecessor
     * @return new version label
     */
    private String createNewVersionLabel(String lastDescendantVersion) {
        String predecessorVersion = cmnVersion;

        /* Note: this is somewhat non-intuitive code which I have inherited without documentation,
         * so let's take it step by step.
         *
         * This method is called by createNewVersion which generates a new version of *this*
         * object, so *this* object is the predecessor.
         *
         * Simple cmnVersion is a monotonically increasing integer 1 ..n.
         * It only increases by one from version to version.
         * Simple versions belong to the main trunk. (So, trunk and branches)
         *
         * Cinnamon supports branching, so you can create a new object based on an earlier
         * version instead. For example, if you have version 1, 2 and 3, you can create a
         * new branch based on version 2. A new branch is created when you version an object
         * that already has a descendant version. If you create an object without a descendant,
         * the current version is simply increased by 1.
         *
         * The version numbers of branches are constructed by using the predecessor version
         * number (in this example: 2), adding a "." and the branch counter (1 for the first
         * branch) and a hyphen for the new version of the first object in this branch:
         * Example 1:
         * 2.1-1
         *  - 2: base version in main trunk
         *  - "." as separator
         *  - version 1 for first branch
         *  - "-" as separator between branch and version
         *  - version 1 for first object in this new branch.
         *
         * Next version in this branch would be 2.1-2
         *
         * Example 2:
         * A new version based on branch based on 2.1-2 with an existing sibling branch of
         * 2.2-1 will be 2.1-1.1-1
         *  - 2.1-1 "base branch"
         *  - "."
         *  - 1-1 first branch (of base), first version
         *
         * Example 3:
         * A new version based on v2 (where only v3 exists, but no branches of v2 yet)
         *
         * Example 4: new version based on v3 (no descendants, no siblings)
         */

        /*
         * First, split the branches at the branch separator:
         * Example 1: 2.1-1 has predecessorVersion 2 so this becomes [2]
         * Example 2:  same as Ex.1
         * Example 3: 2 becomes [2]
         * Example 4: 3 becomes [3]
         */
        String[] branches = predecessorVersion.split("\\.");

        /*
         * lastSegment is the last version:
         * Example 1: one-element array [2]
         * Example 2: same as Ex.1
         * Example 3: one-element array [2]
         * Example 4: one-element array [3]
         */
        String lastSegment = branches[branches.length - 1];

        /*
         * Example 1: lastBranch is [2]
         * Example 2: sames as Ex.1
         * Example 3: one-element array [2]
         * Example 4: one-element array [3]
         */
        String[] lastBranch = lastSegment.split("-");

        /*
         * The lastDescendantVersion is version of the newest object
         * (the one with the highest id (object ids are monotonically increasing integers, too))
         * which has this object as its predecessor.
         * Example 1: 2.1.1 (assuming 2.1-1 is the first version of the first branch with no other
         * branches of version 2 existing)
         * Example 2: v2.1-1 has lastDescendantVersion v2.2-1
         * Example 3: v2 has lastDescendantVersion v3 (with v1, v2, v3, v3 is lastDescendant of v2)
         * Example 4: null (v3 has no further descendants)
         */
        if (lastDescendantVersion == null) {
            // no object with same predecessor
            String leaf;
            if (lastBranch.length == 2) {
                /*
                 * Example: creating a new version of a branch like 2.1-3 with no siblings:
                 * branches = 2, 1-3
                 * lastSegment = 1-3
                 * two elements in lastBranch [1],[3]
                 * leaf = 3, new version would be 2.1-4
                 */
                leaf = lastBranch[1];
            } else {
                /*
                 * Example 2: one element in lastBranch; 2
                 * Example 4: there is only one element in lastBranch, "3"
                 */
                leaf = lastBranch[0];
            }

            /*
             * new version on this branch increases by 1
             * Example 2: 2
             * Example 4: 4
             */
            leaf = String.valueOf(Integer.parseInt(leaf) + 1);

            /*
             * Build the new version out of the stem (trunk+branch) + leaf (new version)
             * Example 1: predecessor version for the new version of is 2.1-1, so
             * stem is 2.1- and leaf is 2 -> new version is 2.1-2
             *
             * Example 4: predecessor version is v3, so stem is ''
             * -> new version is '' + 4 = 4
             */
            String stem = predecessorVersion.substring(0, predecessorVersion.length() - leaf.length());
            return stem + leaf;
        }
        /*
         * Example 2: has 2.1-1 has a sibling branch 2.2-1
         * lastDescendant version is 2.2-1 (the sibling is newer)
         * branches would be [2], [1-1]
         * lastDescBranches is [2], [2-1]
         */
        String[] lastDescBranches = lastDescendantVersion.split("\\.");

        /*
         * Example 2: siblings have same branch length
         */
        if (branches.length == lastDescBranches.length) {
            /*
             * predecessorVersion is Ex.2 (2.1-1) and we append the first version of a new branch:
             * -> v2.1-1.1-1 "first version of first branch of 2.1-1"
             */
            return predecessorVersion + ".1-1";
        }

        /*
         * Example: we have version 2.1-1 and want to create a new version of 2, which will be the second branch of 2.
         * - latestDescBranches are 2 and 1-1 (splitting lastDescendant 2.1-1 at ".")
         * - newest descendant branch is 1.1
         * - new branch version should be 2-1 of v2 -> 2.2-1
         */
        String newestDescendantBranch = lastDescBranches[lastDescBranches.length - 1];
        String currentBranchVersion   = newestDescendantBranch.split("-")[0];
        String newBranchVersion       = String.valueOf(Integer.parseInt(currentBranchVersion) + 1);
        return predecessorVersion + "." + newBranchVersion + "-1";
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

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }

    public List<Meta> getMetas() {
        if (metas == null) {
            metas = new ArrayList<>();
        }
        return metas;
    }

    public void setMetas(List<Meta> metas) {
        this.metas = metas;
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
                // objVersion was originally used to detect database level changes for Hibernate in Cinnamon 3.
                // It will change even by osdDao.update() though the object's other fields may remain unchanged.
                // So, change osd.name, change it back, fetch osd anew -> you have two non-equal objects if you compare
                // objVersion.
                // TODO: do we still need objVersion?
                // Objects.equals(objVersion, that.objVersion) &&
                Objects.equals(contentHash, that.contentHash) &&
                Objects.equals(contentProvider, that.contentProvider) &&
                compareMetas(getMetas(), that.getMetas());
    }

    private boolean compareMetas(List<Meta> metas, List<Meta> thatMetas) {
        if (metas.size() != thatMetas.size()) {
            return false;
        }
        metas.sort(Comparator.comparingLong(Meta::getId));
        thatMetas.sort(Comparator.comparingLong(Meta::getId));
        return metas.equals(thatMetas);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, ownerId, created, parentId, cmnVersion);
    }

    @Override
    public String toString() {
        return "ObjectSystemData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contentPath='" + contentPath + '\'' +
                ", contentSize=" + contentSize +
                ", predecessorId=" + predecessorId +
                ", rootId=" + rootId +
                ", creatorId=" + creatorId +
                ", modifierId=" + modifierId +
                ", ownerId=" + ownerId +
                ", lockerId=" + lockerId +
                ", created=" + created +
                ", modified=" + modified +
                ", languageId=" + languageId +
                ", aclId=" + aclId +
                ", parentId=" + parentId +
                ", formatId=" + formatId +
                ", typeId=" + typeId +
                ", latestHead=" + latestHead +
                ", latestBranch=" + latestBranch +
                ", contentChanged=" + contentChanged +
                ", metadataChanged=" + metadataChanged +
                ", cmnVersion='" + cmnVersion + '\'' +
                ", lifecycleStateId=" + lifecycleStateId +
                ", summary='" + summary + '\'' +
                ", objVersion=" + objVersion +
                ", contentHash='" + contentHash + '\'' +
                ", contentProvider='" + contentProvider + '\'' +
                '}';
    }
}
