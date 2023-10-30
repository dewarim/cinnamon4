package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.CinnamonObject;
import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.content.ContentMetadata;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.provider.DefaultContentProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;

/**
 * Core Cinnamon object, contains the system's data of an object (document, media file, other resource).
 * It's abbreviated to OSD.
 */
@JacksonXmlRootElement(localName = "objectSystemData")
public class ObjectSystemData implements ContentMetadata, CinnamonObject, Identifiable {
    private static final Logger log = LogManager.getLogger(ObjectSystemData.class);

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date created = new Date();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date modified = new Date();
    private Long languageId;
    private Long aclId;
    private Long parentId;
    private Long formatId;
    private Long typeId;

    @JacksonXmlElementWrapper(localName = "metasets")
    @JacksonXmlProperty(localName = "metaset")
    private List<Meta> metas;

    @JacksonXmlElementWrapper(localName = "relations")
    @JacksonXmlProperty(localName = "relation")
    private List<Relation> relations;

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
    private Boolean contentChanged  = false;
    private Boolean metadataChanged = false;
    private String  cmnVersion      = "1";
    private Long    lifecycleStateId;
    private String  summary         = DEFAULT_SUMMARY;

    private String contentHash;

    // Note: also hardcoded use by DeletionTask
    private String contentProvider = DefaultContentProvider.FILE_SYSTEM.name();

    public ObjectSystemData() {
    }

    public ObjectSystemData(Long id, String name, Long ownerId, Long languageId, Long aclId, Long parentId, Long typeId) {
        this.id         = id;
        this.name       = name;
        this.ownerId    = ownerId;
        this.languageId = languageId;
        this.aclId      = aclId;
        this.parentId   = parentId;
        this.typeId     = typeId;
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
//        String nextVersionLabel = createNewVersionLabel(lastDescendantVersion);
        log.debug("create next version from {} with lastDescendantVersion {}", this.getCmnVersion(), lastDescendantVersion);
        CmnVersion version          = new CmnVersion(this.getCmnVersion(), lastDescendantVersion);
        String     nextVersionLabel = version.getMyVersionString();
        log.debug("nextVersionLabel: " + version);
        nextVersion.setCmnVersion(nextVersionLabel);
        return nextVersion;
    }


    /*
     * Note: versioning in Cinnamon is not very intuitive... but it is what it is now.
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
    // use a small inner class so the code is easier to comprehend and debug.
    private static class CmnVersion {

        private        String  myPredecessorVersion;
        private        String  descendantVersion;
        private        long    myVersion   = 0;
        private        String  myBranch    = "";
        private static Pattern MAIN_BRANCH = Pattern.compile("\\d+");

        public CmnVersion(String parentVersion, String lastSiblingOrDescendantVersion) {
            myPredecessorVersion = parentVersion;
            descendantVersion    = Objects.requireNonNullElse(lastSiblingOrDescendantVersion, "");
        }

        public void version() {
            // case 1: new version based on trunk:
            if (MAIN_BRANCH.matcher(myPredecessorVersion).matches()) {
                if (descendantVersion.isEmpty()) {
                    // case 1.a: no descendant, just grow the trunk:
                    myVersion = Long.parseLong(myPredecessorVersion) + 1;
                    myBranch  = "";
                }
                else if (MAIN_BRANCH.matcher(descendantVersion).matches()) {
                    // case 1.b: we have a descendant on trunk, so we create a new branch
                    /*
                     * Example:
                     * v1, v2, new branch based on v1 should be 1.1-1 (trunk . first branch - first version)
                     */
                    myVersion = 1;
                    myBranch  = myPredecessorVersion + ".1";
                }
                else {
                    // case 1c: we have at least 1 sibling branch
                    // -> create a new branch
                    myVersion = 1;
                    myBranch  = increaseBranchNumber(descendantVersion);
                }
            }
            else {
                // case 2: we are on a branch
                if (descendantVersion.isEmpty()) {
                    // case 2.a: we are on a branch but have no siblings: just increase version number
                    myVersion = increaseVersionNumber(myPredecessorVersion);
                    myBranch  = extractBranch(myPredecessorVersion);
                }
                else {
                    // case 2.b: we are on a branch and have a sibling: create a new branch
                    myVersion = 1;
                    myBranch  = myPredecessorVersion + ".1";
                }
            }
        }

        private String extractBranch(String version) {
            int lastVersionSeparator = version.lastIndexOf("-");
            return version.substring(0, lastVersionSeparator);
        }

        private long increaseVersionNumber(String myPredecessorVersion) {
            int  lastVersionSeparator = myPredecessorVersion.lastIndexOf("-");
            long lastVersion          = Long.parseLong(myPredecessorVersion.substring(lastVersionSeparator + 1));
            return lastVersion + 1;
        }

        private String increaseBranchNumber(String myPredecessorVersion) {
            int    lastVersionSeparator    = myPredecessorVersion.lastIndexOf("-");
            int    lastBranchSeparator     = myPredecessorVersion.lastIndexOf(".");
            long   predecessorBranchNumber = Long.parseLong(myPredecessorVersion.substring(lastBranchSeparator + 1, lastVersionSeparator));
            String branch                  = myPredecessorVersion.substring(0, lastBranchSeparator);
            return branch + "." + (predecessorBranchNumber + 1);
        }

        public String getMyVersionString() {
            if (myVersion == 0) {
                version();
            }
            if (myBranch.isEmpty()) {
                return String.valueOf(myVersion);
            }
            return myBranch + "-" + myVersion;
        }

        @Override
        public String toString() {
            return "CmnVersion{" +
                    "myPredecessorVersion='" + myPredecessorVersion + '\'' +
                    ", descendantVersion='" + descendantVersion + '\'' +
                    ", myVersion=" + myVersion +
                    ", myBranch='" + myBranch + '\'' +
                    '}';
        }
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

    public boolean isLatestBranch() {
        return latestBranch;
    }

    public void setLatestBranch(boolean latestBranch) {
        this.latestBranch = latestBranch;
    }

    public Boolean isContentChanged() {
        return contentChanged;
    }

    public void setContentChanged(Boolean contentChanged) {
        this.contentChanged = contentChanged;
    }

    public Boolean isMetadataChanged() {
        return metadataChanged;
    }

    public void setMetadataChanged(Boolean metadataChanged) {
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
                Objects.equals(contentHash, that.contentHash) &&
                Objects.equals(contentProvider, that.contentProvider) &&
                compareMetas(getMetas(), that.getMetas());
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
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
                ", contentHash='" + contentHash + '\'' +
                ", contentProvider='" + contentProvider + '\'' +
                '}';
    }

    public boolean lockedByUser(UserAccount user) {
        return user.getId().equals(lockerId);
    }

    public boolean lockedByOtherUser(UserAccount user) {
        return lockerId != null && !user.getId().equals(lockerId);
    }
}
