package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;
import java.util.Optional;

@JacksonXmlRootElement(localName = "userPermissionRequest")
public class UserPermissionRequest implements ApiRequest<UserPermissionRequest> {

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

    public boolean validated() {
        return userId != null && userId > 0 && aclId != null && aclId > 0;
    }

    public Optional<UserPermissionRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<UserPermissionRequest>> examples() {
        return List.of(new UserPermissionRequest(6L,7L));
    }
}
