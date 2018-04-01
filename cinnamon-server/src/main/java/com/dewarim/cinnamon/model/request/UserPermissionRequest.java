package com.dewarim.cinnamon.model.request;

public class UserPermissionRequest {
    
    private Long userId;
    private Long aclId;

    public UserPermissionRequest(Long userId, Long aclId) {
        this.userId = userId;
        this.aclId = aclId;
    }

    public UserPermissionRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAclId() {
        return aclId;
    }

    public void setAclId(Long aclId) {
        this.aclId = aclId;
    }
}
