package com.dewarim.cinnamon.model.request.user;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.List;
import java.util.Optional;

@JsonRootName("deleteUserAccountRequest")
public record DeleteUserAccountRequest(Long userId, Long assetReceiverId) implements ApiRequest<DeleteUserAccountRequest> {

    private boolean validated() {
        return userId != null && userId > 0 && assetReceiverId != null && assetReceiverId > 0;
    }

    public Optional<DeleteUserAccountRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<DeleteUserAccountRequest>> examples() {
        return List.of(new DeleteUserAccountRequest(4L, 5L));
    }
}
