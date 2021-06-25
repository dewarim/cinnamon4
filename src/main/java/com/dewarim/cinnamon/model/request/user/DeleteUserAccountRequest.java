package com.dewarim.cinnamon.model.request.user;

import java.util.Optional;

public class DeleteUserAccountRequest {

    private Long userId;
    /**
     * Id of the user to whom all assets are transferred.
     * This is necessary so foreign key relations (for example: document.owner) still work after the original owner
     * account has been deleted.
     */
    private Long assetReceiverId;

    public DeleteUserAccountRequest() {
    }

    public DeleteUserAccountRequest(Long userId, Long assetReceiverId) {
        this.userId = userId;
        this.assetReceiverId = assetReceiverId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAssetReceiverId() {
        return assetReceiverId;
    }

    public void setAssetReceiverId(Long assetReceiverId) {
        this.assetReceiverId = assetReceiverId;
    }

    private boolean validated(){
        return userId != null && userId > 0 && assetReceiverId != null && assetReceiverId > 0;
    }


    public Optional<DeleteUserAccountRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }
}
