package com.dewarim.cinnamon.model.request;

import com.dewarim.cinnamon.model.LinkResolver;

public class LinkUpdateRequest {

    private long         id;
    private Long         aclId;
    private Long         parentId;
    private Long         objectId;
    private Long         folderId;
    private LinkResolver resolver;
    private Long         ownerId;

    public LinkUpdateRequest() {
    }

    public LinkUpdateRequest(long id) {
        this.id = id;
    }

    public LinkUpdateRequest(long id, Long aclId, Long parentId, Long objectId, Long folderId, LinkResolver resolver, Long ownerId) {
        this.id = id;
        this.aclId = aclId;
        this.parentId = parentId;
        this.objectId = objectId;
        this.folderId = folderId;
        this.resolver = resolver;
        this.ownerId = ownerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
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

    public boolean validated() {
        if (id < 1) {
            return false;
        }

        // a link can point to only one target:
        if(objectId != null && folderId != null){
            return false;
        }
        
        boolean atLeastOneFieldIsSet = false;
        Long[] fields = {aclId, parentId, objectId, folderId, ownerId};
        for (Long field : fields) {
            if(field != null){
                if(field < 1){
                    return false;
                }
                atLeastOneFieldIsSet=true;
            }
        }

        if(resolver != null){
            atLeastOneFieldIsSet = true;
        }
        
        return atLeastOneFieldIsSet;
    }

    @Override
    public String toString() {
        return "LinkUpdateRequest{" +
               "id=" + id +
               ", aclId=" + aclId +
               ", parentId=" + parentId +
               ", objectId=" + objectId +
               ", folderId=" + folderId +
               ", resolver=" + resolver +
               ", ownerId=" + ownerId +
               '}';
    }
}
