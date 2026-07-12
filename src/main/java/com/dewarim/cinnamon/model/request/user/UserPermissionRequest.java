package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("userPermissionRequest")
public record UserPermissionRequest(Long userId, Long aclId) implements ApiRequest<UserPermissionRequest> {

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
        return List.of(new UserPermissionRequest(6L, 7L));
    }
}
