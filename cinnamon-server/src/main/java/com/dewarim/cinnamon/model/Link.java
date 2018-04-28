package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Accessible;
import com.dewarim.cinnamon.api.Ownable;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "link")
public class Link implements Ownable, Accessible {
    
    private Long id;
    private LinkType type;
    private LinkResolver resolver;
    private Long ownerId;
    private Long aclId;
    private Long parentId;
    private Long folderId;
    private Long objectId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LinkType getType() {
        return type;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public LinkResolver getResolver() {
        return resolver;
    }

    public void setResolver(LinkResolver resolver) {
        this.resolver = resolver;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return type == link.type &&
                resolver == link.resolver &&
                Objects.equals(ownerId, link.ownerId) &&
                Objects.equals(aclId, link.aclId) &&
                Objects.equals(parentId, link.parentId) &&
                Objects.equals(folderId, link.folderId) &&
                Objects.equals(objectId, link.objectId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, resolver, ownerId, aclId, parentId, folderId, objectId);
    }

    @Override
    public String toString() {
        return "Link{" +
                "id=" + id +
                ", type=" + type +
                ", resolver=" + resolver +
                ", ownerId=" + ownerId +
                ", aclId=" + aclId +
                ", parentId=" + parentId +
                ", folderId=" + folderId +
                ", objectId=" + objectId +
                '}';
    }
}
