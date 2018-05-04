package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.links.LinkType;

public class CreateLinkRequest {

    private long id;
    private long parentId;
    private LinkType linkType;
    private long aclId;
    private long ownerId;

    public CreateLinkRequest() {
    }

    public CreateLinkRequest(long id, long parentId, LinkType linkType, long aclId, long ownerId) {
        this.id = id;
        this.parentId = parentId;
        this.linkType = linkType;
        this.aclId = aclId;
        this.ownerId = ownerId;
    }

    public boolean validated() {
        return id > 0 && parentId > 0 && linkType != null && aclId > 0 && ownerId > 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public long getAclId() {
        return aclId;
    }

    public void setAclId(long aclId) {
        this.aclId = aclId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "CreateLinkRequest{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", linkType=" + linkType +
                ", aclId=" + aclId +
                ", ownerId=" + ownerId +
                '}';
    }
}
