package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.LinkResolver;
import com.dewarim.cinnamon.model.LinkType;

public class CreateLinkRequest {

    private long id;
    private long parentId;
    private LinkResolver linkResolver;
    private LinkType linkType;
    private long aclId;

    public CreateLinkRequest() {
    }

    public CreateLinkRequest(long id, long parentId, LinkResolver linkResolver, LinkType linkType, long aclId) {
        this.id = id;
        this.parentId = parentId;
        this.linkResolver = linkResolver;
        this.linkType = linkType;
        this.aclId = aclId;
    }

    public boolean validated() {
        return id > 0 && parentId > 0 && linkResolver != null && linkType != null && aclId > 0
                // only objects are versioned and may have latest_head resolver
                && (linkResolver == LinkResolver.FIXED || (linkResolver == LinkResolver.LATEST_HEAD && linkType == LinkType.OBJECT));
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

    public LinkResolver getLinkResolver() {
        return linkResolver;
    }

    public void setLinkResolver(LinkResolver linkResolver) {
        this.linkResolver = linkResolver;
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

    @Override
    public String toString() {
        return "CreateLinkRequest{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", linkResolver=" + linkResolver +
                ", linkType=" + linkType +
                ", aclId=" + aclId +
                '}';
    }
}
