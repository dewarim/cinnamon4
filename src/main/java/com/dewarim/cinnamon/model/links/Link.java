package com.dewarim.cinnamon.model.links;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.model.response.LinkResponse;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "link")
public class Link implements Ownable, Identifiable {

    private Long         id;
    private LinkType     type;
    private Long         ownerId;
    private Long         aclId;
    private Long         parentId;
    private Long         folderId;
    private Long         objectId;
    private Long         resolvedId;
    private LinkResolver resolver = LinkResolver.FIXED;

    public Link() {
    }

    public Link(Long id, LinkType type, Long ownerId, Long aclId, Long parentId, Long folderId, Long objectId, LinkResolver resolver) {
        this.id       = id;
        this.type     = type;
        this.ownerId  = ownerId;
        this.aclId    = aclId;
        this.parentId = parentId;
        this.folderId = folderId;
        this.objectId = objectId;
        this.resolver = resolver;
    }

    public Link(LinkResponse linkResponse) {
        this.id       = linkResponse.getId();
        this.type     = linkResponse.getType();
        this.ownerId  = linkResponse.getOwnerId();
        this.aclId    = linkResponse.getAclId();
        this.parentId = linkResponse.getParentId();
        this.folderId = linkResponse.getFolderId();
        this.objectId = linkResponse.getObjectId();
        this.resolver = linkResponse.getResolver();
        this.resolvedId = linkResponse.getResolvedId();
    }

    public Long resolveLink(){
        if(getResolver()== LinkResolver.FIXED){
            return getObjectId();
        }
        else{
            return getResolvedId();
        }
    }

    public Long getResolvedId() {
        return resolvedId;
    }

    public void setResolvedId(Long resolvedId) {
        this.resolvedId = resolvedId;
    }

    public LinkResolver getResolver() {
        return resolver;
    }

    public void setResolver(LinkResolver resolver) {
        this.resolver = resolver;
    }

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return type == link.type &&
                Objects.equals(ownerId, link.ownerId) &&
                Objects.equals(aclId, link.aclId) &&
                Objects.equals(parentId, link.parentId) &&
                Objects.equals(folderId, link.folderId) &&
                Objects.equals(objectId, link.objectId) &&
                resolver == link.resolver;
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, ownerId, aclId, parentId, folderId, objectId);
    }

    @Override
    public String toString() {
        return "Link{" +
                "id=" + id +
                ", type=" + type +
                ", ownerId=" + ownerId +
                ", aclId=" + aclId +
                ", parentId=" + parentId +
                ", folderId=" + folderId +
                ", objectId=" + objectId +
                ", resolvedId=" + resolvedId +
                ", resolver=" + resolver +
                '}';
    }

    /**
     * Check if base values look okay - missing id is allowed.
     * Use this to links supplied from external sources (update/create requests)
     *
     * @return true if all values pass basic validity check.
     */
    public boolean validated() {
        if (folderId != null && objectId != null) {
            return false;
        }
        return (id == null || id > 0) && getAclId() != null && getType() != null &&
                ((folderId != null && folderId > 0) || (objectId != null && objectId > 0))
                && aclId > 0 && ownerId > 0
                && parentId != null && parentId > 0;
    }
}
