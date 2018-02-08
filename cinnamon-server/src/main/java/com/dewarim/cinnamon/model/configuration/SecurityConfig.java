package com.dewarim.cinnamon.model.configuration;

public class SecurityConfig {

    private Boolean transferAssetsAllowed = true;
    private Boolean deleteUserAllowed = true;

    public Boolean getTransferAssetsAllowed() {
        return transferAssetsAllowed;
    }

    public void setTransferAssetsAllowed(Boolean transferAssetsAllowed) {
        this.transferAssetsAllowed = transferAssetsAllowed;
    }

    public Boolean getDeleteUserAllowed() {
        return deleteUserAllowed;
    }

    public void setDeleteUserAllowed(Boolean deleteUserAllowed) {
        this.deleteUserAllowed = deleteUserAllowed;
    }
}
